package com.pariksha.academic;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findBySubjectIdOrderByNameAsc(Long subjectId);

    boolean existsBySubjectIdAndNameIgnoreCase(Long subjectId, String name);
}
