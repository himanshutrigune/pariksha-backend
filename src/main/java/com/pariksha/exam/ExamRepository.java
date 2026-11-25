package com.pariksha.exam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    Optional<Exam> findByIdAndInstituteId(Long id, Long instituteId);

    @Query("""
            select e from Exam e
            where e.institute.id = :instituteId
              and (:status is null or e.status = :status)
            order by e.startTime desc
            """)
    Page<Exam> search(Long instituteId, ExamStatus status, Pageable pageable);
}
