package com.pariksha.institute;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituteRepository extends JpaRepository<Institute, Long> {

    boolean existsByCodeIgnoreCase(String code);
}
