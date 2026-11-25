package com.pariksha.auth;

import com.pariksha.user.Role;
import com.pariksha.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterInstituteRequest(
            @NotBlank @Size(max = 255) String instituteName,
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{3,50}$",
                    message = "Code must be 3-50 characters: letters, digits, '-' or '_'") String instituteCode,
            @NotBlank @Size(max = 150) String adminName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
            @Size(max = 20) String mobileNumber) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record UserSummary(Long id, String name, String email, Role role, Long instituteId) {
        public static UserSummary from(User u) {
            return new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getInstitute().getId());
        }
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserSummary user) {
    }
}
