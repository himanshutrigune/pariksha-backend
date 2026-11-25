package com.pariksha.auth;

import com.pariksha.auth.AuthDtos.AuthResponse;
import com.pariksha.auth.AuthDtos.LoginRequest;
import com.pariksha.auth.AuthDtos.RegisterInstituteRequest;
import com.pariksha.auth.AuthDtos.UserSummary;
import com.pariksha.common.ConflictException;
import com.pariksha.common.ResourceNotFoundException;
import com.pariksha.common.UnauthorizedException;
import com.pariksha.institute.Institute;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.security.AuthUser;
import com.pariksha.security.JwtService;
import com.pariksha.user.Role;
import com.pariksha.user.User;
import com.pariksha.user.UserRepository;
import com.pariksha.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Onboarding: creates a new institute together with its first ADMIN user and logs them in. */
    @Transactional
    public AuthResponse registerInstitute(RegisterInstituteRequest request) {
        String code = request.instituteCode().trim().toUpperCase();
        if (instituteRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("Institute code already exists: " + code);
        }
        Institute institute = new Institute();
        institute.setName(request.instituteName().trim());
        institute.setCode(code);
        institute = instituteRepository.save(institute);

        User admin = userService.createUser(institute.getId(), request.adminName(), request.email(),
                request.mobileNumber(), request.password(), Role.ADMIN);
        return tokenFor(admin);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        // same message for both cases so attackers cannot discover which emails exist
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return tokenFor(user);
    }

    public UserSummary me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserSummary.from(user);
    }

    private AuthResponse tokenFor(User user) {
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getRole(), user.getInstitute().getId());
        return new AuthResponse(jwtService.generateToken(authUser), "Bearer",
                jwtService.expiresInSeconds(), UserSummary.from(user));
    }
}
