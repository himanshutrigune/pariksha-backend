package com.pariksha.academic;

import com.pariksha.academic.AcademicDtos.ChapterResponse;
import com.pariksha.academic.AcademicDtos.NamedResponse;
import com.pariksha.common.ConflictException;
import com.pariksha.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicService {

    private final StandardRepository standardRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;

    public List<NamedResponse> listStandards() {
        return standardRepository.findAllByOrderByNameAsc().stream().map(NamedResponse::from).toList();
    }

    @Transactional
    public NamedResponse createStandard(String name) {
        if (standardRepository.existsByNameIgnoreCase(name.trim())) {
            throw new ConflictException("Standard already exists: " + name);
        }
        Standard standard = new Standard();
        standard.setName(name.trim());
        return NamedResponse.from(standardRepository.save(standard));
    }

    public List<NamedResponse> listSubjects() {
        return subjectRepository.findAllByOrderByNameAsc().stream().map(NamedResponse::from).toList();
    }

    @Transactional
    public NamedResponse createSubject(String name) {
        if (subjectRepository.existsByNameIgnoreCase(name.trim())) {
            throw new ConflictException("Subject already exists: " + name);
        }
        Subject subject = new Subject();
        subject.setName(name.trim());
        return NamedResponse.from(subjectRepository.save(subject));
    }

    public List<ChapterResponse> listChapters(Long subjectId) {
        ensureSubjectExists(subjectId);
        return chapterRepository.findBySubjectIdOrderByNameAsc(subjectId).stream().map(ChapterResponse::from).toList();
    }

    @Transactional
    public ChapterResponse createChapter(Long subjectId, String name) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));
        if (chapterRepository.existsBySubjectIdAndNameIgnoreCase(subjectId, name.trim())) {
            throw new ConflictException("Chapter already exists in this subject: " + name);
        }
        Chapter chapter = new Chapter();
        chapter.setSubject(subject);
        chapter.setName(name.trim());
        return ChapterResponse.from(chapterRepository.save(chapter));
    }

    private void ensureSubjectExists(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found: " + subjectId);
        }
    }
}
