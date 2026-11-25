package com.pariksha.student;

import com.pariksha.common.PageResponse;
import com.pariksha.security.AuthUser;
import com.pariksha.student.StudentDtos.CreateStudentRequest;
import com.pariksha.student.StudentDtos.StudentResponse;
import com.pariksha.student.StudentDtos.UpdateStudentRequest;
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
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
@Tag(name = "Students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@AuthenticationPrincipal AuthUser user,
                                  @Valid @RequestBody CreateStudentRequest request) {
        return studentService.create(user.instituteId(), request);
    }

    @GetMapping
    public PageResponse<StudentResponse> search(@AuthenticationPrincipal AuthUser user,
                                                @RequestParam(required = false) String search,
                                                @PageableDefault(size = 10) Pageable pageable) {
        return studentService.search(user.instituteId(), search, pageable);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return studentService.get(user.instituteId(), id);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                  @Valid @RequestBody UpdateStudentRequest request) {
        return studentService.update(user.instituteId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        studentService.deactivate(user.instituteId(), id);
    }
}
