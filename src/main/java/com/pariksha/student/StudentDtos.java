package com.pariksha.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class StudentDtos {

    private StudentDtos() {
    }

    public record CreateStudentRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
            @Size(max = 20) String mobileNumber,
            @NotBlank @Size(max = 30) String rollNumber,
            @Size(max = 11) String gender,
            @Size(max = 20) String parentContact,
            Long standardId,
            LocalDate joiningDate) {
    }

    public record UpdateStudentRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 20) String mobileNumber,
            @NotBlank @Size(max = 30) String rollNumber,
            @Size(max = 11) String gender,
            @Size(max = 20) String parentContact,
            Long standardId) {
    }

    public record StudentResponse(Long id, Long userId, String name, String email, String mobileNumber,
                                  String rollNumber, String gender, String parentContact,
                                  Long standardId, String standardName, LocalDate joiningDate, boolean active) {
        public static StudentResponse from(Student s) {
            return new StudentResponse(
                    s.getId(),
                    s.getUser().getId(),
                    s.getUser().getName(),
                    s.getUser().getEmail(),
                    s.getUser().getMobileNumber(),
                    s.getRollNumber(),
                    s.getGender(),
                    s.getParentContact(),
                    s.getStandard() == null ? null : s.getStandard().getId(),
                    s.getStandard() == null ? null : s.getStandard().getName(),
                    s.getJoiningDate(),
                    s.isActive());
        }
    }
}
