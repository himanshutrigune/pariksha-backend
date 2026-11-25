package com.pariksha.academic;

import com.pariksha.academic.AcademicDtos.ChapterResponse;
import com.pariksha.academic.AcademicDtos.NameRequest;
import com.pariksha.academic.AcademicDtos.NamedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Standards, Subjects & Chapters")
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/standards")
    public List<NamedResponse> standards() {
        return academicService.listStandards();
    }

    @PostMapping("/standards")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public NamedResponse createStandard(@Valid @RequestBody NameRequest request) {
        return academicService.createStandard(request.name());
    }

    @GetMapping("/subjects")
    public List<NamedResponse> subjects() {
        return academicService.listSubjects();
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public NamedResponse createSubject(@Valid @RequestBody NameRequest request) {
        return academicService.createSubject(request.name());
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public List<ChapterResponse> chapters(@PathVariable Long subjectId) {
        return academicService.listChapters(subjectId);
    }

    @PostMapping("/subjects/{subjectId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ChapterResponse createChapter(@PathVariable Long subjectId, @Valid @RequestBody NameRequest request) {
        return academicService.createChapter(subjectId, request.name());
    }
}
