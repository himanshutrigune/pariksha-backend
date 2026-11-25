package com.pariksha.auth;

import com.pariksha.auth.AuthDtos.AuthResponse;
import com.pariksha.auth.AuthDtos.LoginRequest;
import com.pariksha.auth.AuthDtos.RegisterInstituteRequest;
import com.pariksha.auth.AuthDtos.UserSummary;
import com.pariksha.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-institute")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new institute with its first admin account")
    public AuthResponse registerInstitute(@Valid @RequestBody RegisterInstituteRequest request) {
        return authService.registerInstitute(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT access token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Details of the logged-in user")
    public UserSummary me(@AuthenticationPrincipal AuthUser user) {
        return authService.me(user.userId());
    }
}
