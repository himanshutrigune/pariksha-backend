package com.pariksha.seed;

import com.pariksha.academic.Chapter;
import com.pariksha.academic.ChapterRepository;
import com.pariksha.academic.Standard;
import com.pariksha.academic.StandardRepository;
import com.pariksha.academic.Subject;
import com.pariksha.academic.SubjectRepository;
import com.pariksha.classroom.Classroom;
import com.pariksha.classroom.ClassroomRepository;
import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamQuestion;
import com.pariksha.exam.ExamRepository;
import com.pariksha.exam.ExamStatus;
import com.pariksha.institute.Institute;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.question.Difficulty;
import com.pariksha.question.Question;
import com.pariksha.question.QuestionOption;
import com.pariksha.question.QuestionRepository;
import com.pariksha.student.Student;
import com.pariksha.student.StudentRepository;
import com.pariksha.user.Role;
import com.pariksha.user.User;
import com.pariksha.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Creates a ready-to-use demo institute the first time the app starts on an empty database,
 * so you can open Swagger UI and try every API immediately.
 * Disable with SEED_DEMO_DATA=false (e.g. in production).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private final InstituteRepository instituteRepository;
    private final UserService userService;
    private final StudentRepository studentRepository;
    private final StandardRepository standardRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ClassroomRepository classroomRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void run(String... args) {
        if (instituteRepository.count() > 0) {
            return;
        }
        log.info("Empty database detected - creating demo data ...");

        Institute institute = new Institute();
        institute.setName("Demo Academy");
        institute.setCode("DEMO");
        institute = instituteRepository.save(institute);
        Long instituteId = institute.getId();

        userService.createUser(instituteId, "Demo Admin", "admin@demo.com", "9000000001", "Admin@123", Role.ADMIN);
        User teacher = userService.createUser(instituteId, "Demo Teacher", "teacher@demo.com", "9000000002",
                "Teacher@123", Role.TEACHER);

        Standard class10 = standardRepository.findAllByOrderByNameAsc().stream()
                .filter(s -> s.getName().equals("Class 10")).findFirst().orElse(null);
        Subject maths = subjectRepository.findByNameIgnoreCase("Mathematics").orElseThrow();
        Chapter algebra = chapterRepository.findBySubjectIdOrderByNameAsc(maths.getId()).stream()
                .filter(c -> c.getName().equals("Algebra")).findFirst().orElse(null);

        // ---- students
        Classroom classroom = new Classroom();
        classroom.setInstitute(institute);
        classroom.setName("Class 10 - A");
        for (int i = 1; i <= 5; i++) {
            User user = userService.createUser(instituteId, "Student " + i, "student" + i + "@demo.com",
                    "900000010" + i, "Student@123", Role.STUDENT);
            Student student = new Student();
            student.setInstitute(institute);
            student.setUser(user);
            student.setStandard(class10);
            student.setRollNumber("10A-0" + i);
            student.setGender(i % 2 == 0 ? "FEMALE" : "MALE");
            student.setJoiningDate(LocalDate.now(clock));
            classroom.getStudents().add(studentRepository.save(student));
        }

        // ---- question bank
        Question q1 = question(institute, teacher, maths, algebra, class10, Difficulty.EASY,
                "Solve for x: 2x + 3 = 11", "2x = 8, so x = 4.", 2, "2", "3", "4", "5");
        Question q2 = question(institute, teacher, maths, null, class10, Difficulty.EASY,
                "What is the sum of the interior angles of a triangle?", "It is always 180 degrees.",
                1, "90", "180", "270", "360");
        Question q3 = question(institute, teacher, maths, algebra, class10, Difficulty.MEDIUM,
                "What are the roots of x^2 - 5x + 6 = 0?", "Factorise: (x - 2)(x - 3) = 0.",
                0, "2 and 3", "1 and 6", "-2 and -3", "0 and 5");
        Question q4 = question(institute, teacher, maths, null, class10, Difficulty.MEDIUM,
                "What is the value of sin 30 degrees?", "sin 30 = 1/2.", 0, "1/2", "1", "0", "sqrt(3)/2");
        Question q5 = question(institute, teacher, maths, null, class10, Difficulty.HARD,
                "What is the probability of getting a head in one toss of a fair coin?",
                "Two equally likely outcomes, one of them is a head.", 1, "1/4", "1/2", "1", "0");

        // ---- a published exam, assigned to the classroom
        LocalDateTime now = LocalDateTime.now(clock);
        Exam exam = new Exam();
        exam.setInstitute(institute);
        exam.setCreatedBy(teacher.getId());
        exam.setName("Mathematics Unit Test 1");
        exam.setDescription("Algebra, geometry and probability basics");
        exam.setDurationMinutes(30);
        exam.setStartTime(now.minusDays(1));
        exam.setEndTime(now.plusDays(30));
        exam.setShowResultImmediately(true);
        exam.setStatus(ExamStatus.PUBLISHED);

        int number = 1;
        for (Question q : new Question[]{q1, q2, q3, q4, q5}) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExam(exam);
            eq.setQuestion(q);
            eq.setQuestionNumber(number++);
            eq.setPositiveMarks(4);
            eq.setNegativeMarks(1);
            exam.getQuestions().add(eq);
        }
        exam.setTotalMarks(20);
        exam = examRepository.save(exam);

        classroom.getExams().add(exam);
        classroomRepository.save(classroom);

        log.info("""

                ============================ DEMO DATA READY ============================
                 Swagger UI : http://localhost:8080/swagger-ui.html
                 ADMIN      : admin@demo.com     / Admin@123
                 TEACHER    : teacher@demo.com   / Teacher@123
                 STUDENT    : student1@demo.com  / Student@123   (student1 .. student5)
                =========================================================================""");
    }

    private Question question(Institute institute, User creator, Subject subject, Chapter chapter, Standard standard,
                              Difficulty difficulty, String content, String explanation,
                              int correctIndex, String... options) {
        Question q = new Question();
        q.setInstitute(institute);
        q.setCreatedBy(creator.getId());
        q.setSubject(subject);
        q.setChapter(chapter);
        q.setStandard(standard);
        q.setDifficulty(difficulty);
        q.setContent(content);
        q.setExplanation(explanation);
        for (int i = 0; i < options.length; i++) {
            QuestionOption option = new QuestionOption();
            option.setOptionText(options[i]);
            option.setCorrect(i == correctIndex);
            option.setOptionOrder(i + 1);
            q.addOption(option);
        }
        return questionRepository.save(q);
    }
}
