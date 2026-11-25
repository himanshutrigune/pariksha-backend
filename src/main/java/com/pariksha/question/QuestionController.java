package com.pariksha.question;

import com.pariksha.common.PageResponse;
import com.pariksha.question.QuestionDtos.QuestionRequest;
import com.pariksha.question.QuestionDtos.QuestionResponse;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Tag(name = "Question Bank")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@AuthenticationPrincipal AuthUser user,
                                   @Valid @RequestBody QuestionRequest request) {
        return questionService.create(user.instituteId(), user.userId(), request);
    }

    @GetMapping
    public PageResponse<QuestionResponse> search(@AuthenticationPrincipal AuthUser user,
                                                 @RequestParam(required = false) Long subjectId,
                                                 @RequestParam(required = false) Long chapterId,
                                                 @RequestParam(required = false) Difficulty difficulty,
                                                 @RequestParam(required = false) String search,
                                                 @PageableDefault(size = 10) Pageable pageable) {
        return questionService.search(user.instituteId(), subjectId, chapterId, difficulty, search, pageable);
    }

    @GetMapping("/{id}")
    public QuestionResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return questionService.get(user.instituteId(), id);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                   @Valid @RequestBody QuestionRequest request) {
        return questionService.update(user.instituteId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        questionService.delete(user.instituteId(), id);
    }
}
