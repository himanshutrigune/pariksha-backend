package com.pariksha.user;

import com.pariksha.common.ConflictException;
import com.pariksha.common.PageResponse;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.user.UserDtos.CreateTeacherRequest;
import com.pariksha.user.UserDtos.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createTeacher(Long instituteId, CreateTeacherRequest request) {
        User teacher = createUser(instituteId, request.name(), request.email(),
                request.mobileNumber(), request.password(), Role.TEACHER);
        return UserResponse.from(teacher);
    }

    public PageResponse<UserResponse> listTeachers(Long instituteId, Pageable pageable) {
        return PageResponse.of(
                userRepository.findByInstituteIdAndRoleAndActiveTrue(instituteId, Role.TEACHER, pageable),
                UserResponse::from);
    }

    /** Shared by teacher / student / admin creation so e-mail uniqueness and hashing live in one place. */
    @Transactional
    public User createUser(Long instituteId, String name, String email, String mobile, String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered: " + normalizedEmail);
        }
        User user = new User();
        user.setInstitute(instituteRepository.getReferenceById(instituteId));
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setMobileNumber(mobile);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }
}
