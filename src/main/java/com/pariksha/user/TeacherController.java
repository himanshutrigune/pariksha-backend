package com.pariksha.user;

import com.pariksha.common.PageResponse;
import com.pariksha.security.AuthUser;
import com.pariksha.user.UserDtos.CreateTeacherRequest;
import com.pariksha.user.UserDtos.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Teachers")
public class TeacherController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal AuthUser user,
                               @Valid @RequestBody CreateTeacherRequest request) {
        return userService.createTeacher(user.instituteId(), request);
    }

    @GetMapping
    public PageResponse<UserResponse> list(@AuthenticationPrincipal AuthUser user,
                                           @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return userService.listTeachers(user.instituteId(), pageable);
    }
}
