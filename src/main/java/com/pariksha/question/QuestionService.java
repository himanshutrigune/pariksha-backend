package com.pariksha.question;

import com.pariksha.academic.Chapter;
import com.pariksha.academic.ChapterRepository;
import com.pariksha.academic.Standard;
import com.pariksha.academic.StandardRepository;
import com.pariksha.academic.Subject;
import com.pariksha.academic.SubjectRepository;
import com.pariksha.common.BadRequestException;
import com.pariksha.common.PageResponse;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.question.QuestionDtos.OptionRequest;
import com.pariksha.question.QuestionDtos.QuestionRequest;
import com.pariksha.question.QuestionDtos.QuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final StandardRepository standardRepository;
    private final InstituteRepository instituteRepository;

    @Transactional
    public QuestionResponse create(Long instituteId, Long userId, QuestionRequest request) {
        Question question = new Question();
        question.setInstitute(instituteRepository.getReferenceById(instituteId));
        question.setCreatedBy(userId);
        apply(question, request);
        return QuestionResponse.from(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse update(Long instituteId, Long id, QuestionRequest request) {
        Question question = find(instituteId, id);
        apply(question, request);
        return QuestionResponse.from(question);
    }

    public QuestionResponse get(Long instituteId, Long id) {
        return QuestionResponse.from(find(instituteId, id));
    }

    public PageResponse<QuestionResponse> search(Long instituteId, Long subjectId, Long chapterId,
                                                 Difficulty difficulty, String search, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return PageResponse.of(
                questionRepository.search(instituteId, subjectId, chapterId, difficulty, term, pageable),
                QuestionResponse::from);
    }

    /** Soft delete so old exams that already used this question keep working. */
    @Transactional
    public void delete(Long instituteId, Long id) {
        find(instituteId, id).setActive(false);
    }

    // ---------------------------------------------------------------- helpers

    private Question find(Long instituteId, Long id) {
        return questionRepository.findByIdAndInstituteIdAndActiveTrue(id, instituteId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    private void apply(Question question, QuestionRequest request) {
        validateOptions(request.options());

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + request.subjectId()));
        Chapter chapter = null;
        if (request.chapterId() != null) {
            chapter = chapterRepository.findById(request.chapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + request.chapterId()));
            if (!chapter.getSubject().getId().equals(subject.getId())) {
                throw new BadRequestException("Chapter does not belong to the selected subject");
            }
        }
        Standard standard = null;
        if (request.standardId() != null) {
            standard = standardRepository.findById(request.standardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Standard not found: " + request.standardId()));
        }

        question.setSubject(subject);
        question.setChapter(chapter);
        question.setStandard(standard);
        question.setContent(request.content().trim());
        question.setDifficulty(request.difficulty());
        question.setExplanation(request.explanation());

        // replace all options (orphanRemoval deletes the old rows)
        question.getOptions().clear();
        int order = 1;
        for (OptionRequest o : request.options()) {
            QuestionOption option = new QuestionOption();
            option.setOptionText(o.text().trim());
            option.setCorrect(o.correct());
            option.setOptionOrder(order++);
            question.addOption(option);
        }
    }

    private void validateOptions(List<OptionRequest> options) {
        long correct = options.stream().filter(OptionRequest::correct).count();
        if (correct != 1) {
            throw new BadRequestException("Exactly one option must be marked correct (found " + correct + ")");
        }
    }
}
