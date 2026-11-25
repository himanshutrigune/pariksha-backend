package com.pariksha.attempt;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    Optional<ExamAttempt> findByExamIdAndStudentId(Long examId, Long studentId);

    List<ExamAttempt> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"exam"})
    List<ExamAttempt> findByStudentIdAndStatusOrderBySubmittedAtDesc(Long studentId, AttemptStatus status);

    long countByExamIdAndStatus(Long examId, AttemptStatus status);

    long countByExamIdAndStatusAndTotalScoreGreaterThan(Long examId, AttemptStatus status, double totalScore);

    /** Best score first; if scores tie the student who submitted earlier is listed first. */
    @EntityGraph(attributePaths = {"student", "student.user"})
    @Query("""
            select a from ExamAttempt a
            where a.exam.id = :examId and a.status = :status
            order by a.totalScore desc, a.submittedAt asc
            """)
    List<ExamAttempt> findLeaderboard(Long examId, AttemptStatus status);

    /** Per question: [questionId, number of students who attempted it, number who got it right]. */
    @Query("""
            select a.question.id,
                   sum(case when a.selectedOption is not null then 1 else 0 end),
                   sum(case when a.correct = true then 1 else 0 end)
            from AttemptAnswer a
            where a.attempt.exam.id = :examId and a.attempt.status = :status
            group by a.question.id
            """)
    List<Object[]> questionStats(Long examId, AttemptStatus status);
}
