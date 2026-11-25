package com.pariksha.classroom;

import com.pariksha.exam.Exam;
import com.pariksha.exam.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByIdAndInstituteIdAndActiveTrue(Long id, Long instituteId);

    List<Classroom> findByInstituteIdAndActiveTrueOrderByNameAsc(Long instituteId);

    boolean existsByInstituteIdAndNameIgnoreCase(Long instituteId, String name);

    /** All exams of the given status that were assigned to any classroom this student belongs to. */
    @Query("""
            select distinct e from Classroom c join c.students s join c.exams e
            where s.id = :studentId and c.active = true and e.status = :status
            """)
    List<Exam> findExamsForStudent(Long studentId, ExamStatus status);

    /** > 0 means the student is allowed to take this exam. */
    @Query("""
            select count(c) from Classroom c join c.students s join c.exams e
            where s.id = :studentId and e.id = :examId and c.active = true
            """)
    long countAssignments(Long studentId, Long examId);
}
