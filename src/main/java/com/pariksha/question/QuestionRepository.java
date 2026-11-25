package com.pariksha.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            select q from Question q
            where q.institute.id = :instituteId
              and q.active = true
              and (:subjectId is null or q.subject.id = :subjectId)
              and (:chapterId is null or q.chapter.id = :chapterId)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:search is null or lower(q.content) like lower(concat('%', :search, '%')))
            order by q.id desc
            """)
    Page<Question> search(Long instituteId, Long subjectId, Long chapterId,
                          Difficulty difficulty, String search, Pageable pageable);

    Optional<Question> findByIdAndInstituteIdAndActiveTrue(Long id, Long instituteId);

    List<Question> findByIdInAndInstituteIdAndActiveTrue(Collection<Long> ids, Long instituteId);
}
