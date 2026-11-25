package com.pariksha.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateTeacherRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
            @Size(max = 20) String mobileNumber) {
    }

    public record UserResponse(Long id, String name, String email, String mobileNumber, Role role, boolean active) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getMobileNumber(), u.getRole(), u.isActive());
        }
    }
}
