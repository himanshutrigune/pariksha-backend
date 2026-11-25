package com.pariksha.attempt;

import com.pariksha.attempt.AttemptDtos.AnswerRequest;
import com.pariksha.attempt.AttemptDtos.AnswerReview;
import com.pariksha.attempt.AttemptDtos.Availability;
import com.pariksha.attempt.AttemptDtos.ExamPaperResponse;
import com.pariksha.attempt.AttemptDtos.PaperOption;
import com.pariksha.attempt.AttemptDtos.PaperQuestion;
import com.pariksha.attempt.AttemptDtos.ResultDetail;
import com.pariksha.attempt.AttemptDtos.ResultSummary;
import com.pariksha.attempt.AttemptDtos.ReviewOption;
import com.pariksha.attempt.AttemptDtos.StudentExamSummary;
import com.pariksha.attempt.AttemptDtos.SubmissionResponse;
import com.pariksha.attempt.AttemptDtos.SubmitRequest;
import com.pariksha.classroom.ClassroomRepository;
import com.pariksha.common.BadRequestException;
import com.pariksha.common.ConflictException;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamQuestion;
import com.pariksha.exam.ExamRepository;
import com.pariksha.exam.ExamStatus;
import com.pariksha.question.Question;
import com.pariksha.question.QuestionOption;
import com.pariksha.security.AuthUser;
import com.pariksha.student.Student;
import com.pariksha.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Everything a STUDENT does: see assigned exams, take an exam, submit, view results. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentExamService {

    private final StudentRepository studentRepository;
    private final ExamRepository examRepository;
    private final ClassroomRepository classroomRepository;
    private final ExamAttemptRepository attemptRepository;
    private final ScoringService scoringService;
    private final Clock clock;

    @Value("${app.exam.submit-grace-seconds:60}")
    private long graceSeconds;

    // ------------------------------------------------------------------ list assigned exams

    public List<StudentExamSummary> myExams(AuthUser user) {
        Student student = studentOf(user);
        LocalDateTime now = now();
        Map<Long, ExamAttempt> attemptsByExam = attemptRepository.findByStudentId(student.getId()).stream()
                .collect(Collectors.toMap(a -> a.getExam().getId(), Function.identity()));

        return classroomRepository.findExamsForStudent(student.getId(), ExamStatus.PUBLISHED).stream()
                .sorted(Comparator.comparing(Exam::getStartTime).reversed())
                .map(e -> {
                    ExamAttempt attempt = attemptsByExam.get(e.getId());
                    return new StudentExamSummary(e.getId(), e.getName(), e.getDescription(), e.getStartTime(),
                            e.getEndTime(), e.getDurationMinutes(), e.getTotalMarks(), e.getQuestions().size(),
                            availability(e, now),
                            attempt == null ? "NOT_STARTED" : attempt.getStatus().name());
                })
                .toList();
    }

    // ------------------------------------------------------------------ start (or resume) an exam

    @Transactional
    public ExamPaperResponse start(AuthUser user, Long examId) {
        Student student = studentOf(user);
        Exam exam = assignedExam(student, examId);
        LocalDateTime now = now();

        if (now.isBefore(exam.getStartTime())) {
            throw new BadRequestException("This exam has not started yet. It opens at " + exam.getStartTime());
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new BadRequestException("This exam window is closed");
        }

        ExamAttempt attempt = attemptRepository.findByExamIdAndStudentId(examId, student.getId()).orElse(null);
        if (attempt == null) {
            attempt = new ExamAttempt();
            attempt.setExam(exam);
            attempt.setStudent(student);
            attempt.setStartedAt(now);
            attempt = attemptRepository.save(attempt);
        } else if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new ConflictException("You have already submitted this exam");
        }
        // IN_PROGRESS -> student refreshed the page / lost connection: simply resume with the same deadline

        LocalDateTime deadline = deadline(exam, attempt);
        List<PaperQuestion> questions = exam.getQuestions().stream()
                .map(eq -> new PaperQuestion(
                        eq.getQuestion().getId(),
                        eq.getQuestionNumber(),
                        eq.getQuestion().getContent(),
                        eq.getPositiveMarks(),
                        eq.getNegativeMarks(),
                        eq.getQuestion().getOptions().stream()
                                .map(o -> new PaperOption(o.getId(), o.getOptionText())).toList()))
                .toList();

        return new ExamPaperResponse(attempt.getId(), exam.getId(), exam.getName(), attempt.getStartedAt(),
                deadline, Math.max(0, Duration.between(now, deadline).getSeconds()), questions);
    }

    // ------------------------------------------------------------------ submit + auto evaluation

    @Transactional
    public SubmissionResponse submit(AuthUser user, Long examId, SubmitRequest request) {
        Student student = studentOf(user);
        Exam exam = assignedExam(student, examId);
        ExamAttempt attempt = attemptRepository.findByExamIdAndStudentId(examId, student.getId())
                .orElseThrow(() -> new BadRequestException("You have not started this exam"));
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new ConflictException("You have already submitted this exam");
        }

        LocalDateTime now = now();
        if (now.isAfter(deadline(exam, attempt).plusSeconds(graceSeconds))) {
            throw new BadRequestException("Time is over - the submission window has closed");
        }

        Map<Long, ExamQuestion> examQuestions = exam.getQuestions().stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), Function.identity()));
        Map<Long, AnswerRequest> given = new HashMap<>();
        for (AnswerRequest a : request.answers()) {
            if (!examQuestions.containsKey(a.questionId())) {
                throw new BadRequestException("Question " + a.questionId() + " is not part of this exam");
            }
            if (given.put(a.questionId(), a) != null) {
                throw new BadRequestException("Duplicate answer for question " + a.questionId());
            }
        }

        double total = 0;
        int correct = 0, wrong = 0, skipped = 0;
        for (ExamQuestion eq : exam.getQuestions()) {
            Question question = eq.getQuestion();
            AnswerRequest answer = given.get(question.getId());

            QuestionOption selected = null;
            if (answer != null && answer.selectedOptionId() != null) {
                Long optionId = answer.selectedOptionId();
                selected = question.getOptions().stream()
                        .filter(o -> o.getId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException(
                                "Option " + optionId + " does not belong to question " + question.getId()));
            }

            boolean attempted = selected != null;
            boolean isCorrect = attempted && selected.isCorrect();
            double marks = scoringService.marksFor(attempted, isCorrect, eq.getPositiveMarks(), eq.getNegativeMarks());

            if (!attempted) skipped++;
            else if (isCorrect) correct++;
            else wrong++;
            total += marks;

            AttemptAnswer row = new AttemptAnswer();
            row.setAttempt(attempt);
            row.setQuestion(question);
            row.setSelectedOption(selected);
            row.setCorrect(isCorrect);
            row.setMarksAwarded(marks);
            row.setTimeSpentSeconds(answer == null || answer.timeSpentSeconds() == null ? 0 : answer.timeSpentSeconds());
            attempt.getAnswers().add(row);
        }

        attempt.setTotalScore(total);
        attempt.setCorrectCount(correct);
        attempt.setWrongCount(wrong);
        attempt.setUnattemptedCount(skipped);
        attempt.setSubmittedAt(now);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);

        if (exam.isShowResultImmediately()) {
            return new SubmissionResponse(attempt.getId(), "Exam submitted successfully", summary(attempt, exam));
        }
        return new SubmissionResponse(attempt.getId(),
                "Exam submitted successfully. Results will be available after the exam ends.", null);
    }

    // ------------------------------------------------------------------ results

    public List<ResultSummary> myResults(AuthUser user) {
        Student student = studentOf(user);
        LocalDateTime now = now();
        return attemptRepository.findByStudentIdAndStatusOrderBySubmittedAtDesc(student.getId(), AttemptStatus.SUBMITTED)
                .stream()
                .filter(a -> resultVisible(a.getExam(), now))
                .map(a -> summary(a, a.getExam()))
                .toList();
    }

    public ResultDetail myResult(AuthUser user, Long examId) {
        Student student = studentOf(user);
        ExamAttempt attempt = attemptRepository.findByExamIdAndStudentId(examId, student.getId())
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED)
                .orElseThrow(() -> new ResourceNotFoundException("No submitted attempt found for exam " + examId));
        Exam exam = attempt.getExam();
        if (!resultVisible(exam, now())) {
            throw new AccessDeniedException("Result is not published yet");
        }

        Map<Long, Integer> numbers = exam.getQuestions().stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), ExamQuestion::getQuestionNumber));

        List<AnswerReview> review = attempt.getAnswers().stream()
                .map(a -> new AnswerReview(
                        numbers.get(a.getQuestion().getId()),
                        a.getQuestion().getId(),
                        a.getQuestion().getContent(),
                        a.getQuestion().getOptions().stream()
                                .map(o -> new ReviewOption(o.getId(), o.getOptionText(), o.isCorrect())).toList(),
                        a.getSelectedOption() == null ? null : a.getSelectedOption().getId(),
                        a.isCorrect(),
                        a.getMarksAwarded(),
                        a.getTimeSpentSeconds(),
                        a.getQuestion().getExplanation()))
                .sorted(Comparator.comparingInt(AnswerReview::questionNumber))
                .toList();
        return new ResultDetail(summary(attempt, exam), review);
    }

    // ------------------------------------------------------------------ helpers

    private Student studentOf(AuthUser user) {
        return studentRepository.findByUserId(user.userId())
                .filter(Student::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for this account"));
    }

    /** Exam must be published AND assigned (through a classroom) to this student. */
    private Exam assignedExam(Student student, Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        if (exam.getStatus() != ExamStatus.PUBLISHED
                || classroomRepository.countAssignments(student.getId(), examId) == 0) {
            throw new AccessDeniedException("This exam is not assigned to you");
        }
        return exam;
    }

    private ResultSummary summary(ExamAttempt attempt, Exam exam) {
        int rank = (int) attemptRepository.countByExamIdAndStatusAndTotalScoreGreaterThan(
                exam.getId(), AttemptStatus.SUBMITTED, attempt.getTotalScore()) + 1;
        return new ResultSummary(attempt.getId(), exam.getId(), exam.getName(), attempt.getTotalScore(),
                exam.getTotalMarks(), scoringService.percentage(attempt.getTotalScore(), exam.getTotalMarks()),
                attempt.getCorrectCount(), attempt.getWrongCount(), attempt.getUnattemptedCount(),
                rank, attempt.getSubmittedAt());
    }

    /** Finish time = the earlier of (start + duration) and the exam window end. */
    private LocalDateTime deadline(Exam exam, ExamAttempt attempt) {
        LocalDateTime byDuration = attempt.getStartedAt().plusMinutes(exam.getDurationMinutes());
        return byDuration.isBefore(exam.getEndTime()) ? byDuration : exam.getEndTime();
    }

    private Availability availability(Exam exam, LocalDateTime now) {
        if (now.isBefore(exam.getStartTime())) return Availability.UPCOMING;
        if (now.isAfter(exam.getEndTime())) return Availability.ENDED;
        return Availability.LIVE;
    }

    private boolean resultVisible(Exam exam, LocalDateTime now) {
        return exam.isShowResultImmediately() || now.isAfter(exam.getEndTime());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
