package com.pariksha.exam;

import com.pariksha.common.BadRequestException;
import com.pariksha.common.PageResponse;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.exam.ExamDtos.ExamDetailResponse;
import com.pariksha.exam.ExamDtos.ExamQuestionRequest;
import com.pariksha.exam.ExamDtos.ExamRequest;
import com.pariksha.exam.ExamDtos.ExamResponse;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.question.Question;
import com.pariksha.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final InstituteRepository instituteRepository;

    @Transactional
    public ExamDetailResponse create(Long instituteId, Long userId, ExamRequest request) {
        Exam exam = new Exam();
        exam.setInstitute(instituteRepository.getReferenceById(instituteId));
        exam.setCreatedBy(userId);
        apply(exam, instituteId, request);
        return ExamDetailResponse.from(examRepository.save(exam));
    }

    /** Only DRAFT exams can be edited. Once published the paper is frozen. */
    @Transactional
    public ExamDetailResponse update(Long instituteId, Long id, ExamRequest request) {
        Exam exam = find(instituteId, id);
        requireDraft(exam, "edited");
        // delete old rows first, otherwise inserts could clash with the unique (exam_id, question_number) key
        exam.getQuestions().clear();
        examRepository.flush();
        apply(exam, instituteId, request);
        return ExamDetailResponse.from(exam);
    }

    public ExamDetailResponse get(Long instituteId, Long id) {
        return ExamDetailResponse.from(find(instituteId, id));
    }

    public PageResponse<ExamResponse> search(Long instituteId, ExamStatus status, Pageable pageable) {
        return PageResponse.of(examRepository.search(instituteId, status, pageable), ExamResponse::from);
    }

    @Transactional
    public ExamResponse publish(Long instituteId, Long id) {
        Exam exam = find(instituteId, id);
        requireDraft(exam, "published");
        if (exam.getQuestions().isEmpty()) {
            throw new BadRequestException("Cannot publish an exam without questions");
        }
        exam.setStatus(ExamStatus.PUBLISHED);
        return ExamResponse.from(exam);
    }

    @Transactional
    public ExamResponse archive(Long instituteId, Long id) {
        Exam exam = find(instituteId, id);
        exam.setStatus(ExamStatus.ARCHIVED);
        return ExamResponse.from(exam);
    }

    @Transactional
    public void delete(Long instituteId, Long id) {
        Exam exam = find(instituteId, id);
        requireDraft(exam, "deleted");
        examRepository.delete(exam);
    }

    // ---------------------------------------------------------------- helpers

    public Exam find(Long instituteId, Long id) {
        return examRepository.findByIdAndInstituteId(id, instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
    }

    private void requireDraft(Exam exam, String action) {
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT exams can be " + action + " (current status: " + exam.getStatus() + ")");
        }
    }

    private void apply(Exam exam, Long instituteId, ExamRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }
        exam.setName(request.name().trim());
        exam.setDescription(request.description());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setStartTime(request.startTime());
        exam.setEndTime(request.endTime());
        exam.setShowResultImmediately(request.showResultImmediately());

        // every question must exist, belong to this institute and appear only once
        Set<Long> ids = new HashSet<>();
        for (ExamQuestionRequest q : request.questions()) {
            if (!ids.add(q.questionId())) {
                throw new BadRequestException("Question added twice: " + q.questionId());
            }
        }
        Map<Long, Question> found = questionRepository.findByIdInAndInstituteIdAndActiveTrue(ids, instituteId)
                .stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        if (found.size() != ids.size()) {
            throw new BadRequestException("One or more questions do not exist in your question bank");
        }

        int number = 1;
        double total = 0;
        List<ExamQuestionRequest> requested = request.questions();
        for (ExamQuestionRequest r : requested) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExam(exam);
            eq.setQuestion(found.get(r.questionId()));
            eq.setQuestionNumber(number++);
            eq.setPositiveMarks(r.positiveMarks());
            eq.setNegativeMarks(r.negativeMarks() == null ? 0 : r.negativeMarks());
            exam.getQuestions().add(eq);
            total += r.positiveMarks();
        }
        exam.setTotalMarks(total);
    }
}
