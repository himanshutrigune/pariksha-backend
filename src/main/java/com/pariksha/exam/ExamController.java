package com.pariksha.exam;

import com.pariksha.common.PageResponse;
import com.pariksha.exam.ExamDtos.ExamDetailResponse;
import com.pariksha.exam.ExamDtos.ExamRequest;
import com.pariksha.exam.ExamDtos.ExamResponse;
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
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Tag(name = "Exams")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamDetailResponse create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody ExamRequest request) {
        return examService.create(user.instituteId(), user.userId(), request);
    }

    @GetMapping
    public PageResponse<ExamResponse> search(@AuthenticationPrincipal AuthUser user,
                                             @RequestParam(required = false) ExamStatus status,
                                             @PageableDefault(size = 10) Pageable pageable) {
        return examService.search(user.instituteId(), status, pageable);
    }

    @GetMapping("/{id}")
    public ExamDetailResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return examService.get(user.instituteId(), id);
    }

    @PutMapping("/{id}")
    public ExamDetailResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                     @Valid @RequestBody ExamRequest request) {
        return examService.update(user.instituteId(), id, request);
    }

    @PostMapping("/{id}/publish")
    public ExamResponse publish(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return examService.publish(user.instituteId(), id);
    }

    @PostMapping("/{id}/archive")
    public ExamResponse archive(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return examService.archive(user.instituteId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        examService.delete(user.instituteId(), id);
    }
}
