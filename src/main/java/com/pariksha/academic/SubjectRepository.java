package com.pariksha.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Subject> findAllByOrderByNameAsc();

    Optional<Subject> findByNameIgnoreCase(String name);
}
