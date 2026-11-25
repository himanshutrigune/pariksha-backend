package com.pariksha.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StandardRepository extends JpaRepository<Standard, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Standard> findAllByOrderByNameAsc();
}
