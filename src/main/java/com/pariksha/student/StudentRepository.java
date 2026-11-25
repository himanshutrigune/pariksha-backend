package com.pariksha.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"user", "standard"})
    @Query("""
            select s from Student s join s.user u
            where s.institute.id = :instituteId
              and s.active = true
              and (:search is null
                   or lower(u.name) like lower(concat('%', :search, '%'))
                   or lower(s.rollNumber) like lower(concat('%', :search, '%')))
            order by u.name
            """)
    Page<Student> search(Long instituteId, String search, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "standard"})
    Optional<Student> findByIdAndInstituteId(Long id, Long instituteId);

    Optional<Student> findByUserId(Long userId);

    boolean existsByInstituteIdAndRollNumberIgnoreCase(Long instituteId, String rollNumber);

    @EntityGraph(attributePaths = {"user"})
    List<Student> findByIdInAndInstituteIdAndActiveTrue(Collection<Long> ids, Long instituteId);
}
