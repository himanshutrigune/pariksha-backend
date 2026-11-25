package com.pariksha;

import com.pariksha.auth.AuthDtos.AuthResponse;
import com.pariksha.auth.AuthDtos.LoginRequest;
import com.pariksha.auth.AuthDtos.RegisterInstituteRequest;
import com.pariksha.auth.AuthService;
import com.pariksha.common.ConflictException;
import com.pariksha.common.UnauthorizedException;
import com.pariksha.institute.Institute;
import com.pariksha.institute.InstituteRepository;
import com.pariksha.security.AuthUser;
import com.pariksha.security.JwtService;
import com.pariksha.user.Role;
import com.pariksha.user.User;
import com.pariksha.user.UserRepository;
import com.pariksha.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit test: every collaborator is a Mockito mock, no Spring context and no database. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock InstituteRepository instituteRepository;
    @Mock UserService userService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private User activeUser() {
        Institute institute = new Institute();
        institute.setName("Demo");
        institute.setCode("DEMO");
        User user = new User();
        user.setInstitute(institute);
        user.setName("Asha");
        user.setEmail("asha@demo.com");
        user.setPasswordHash("hashed");
        user.setRole(Role.TEACHER);
        user.setActive(true);
        return user;
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = activeUser();
        when(userRepository.findByEmailIgnoreCase("asha@demo.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any(AuthUser.class))).thenReturn("jwt-token");
        when(jwtService.expiresInSeconds()).thenReturn(7200L);

        AuthResponse response = authService.login(new LoginRequest("asha@demo.com", "secret123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().role()).isEqualTo(Role.TEACHER);
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("nobody@demo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@demo.com", "whatever1")))
                .isInstanceOf(UnauthorizedException.class);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginFailsForWrongPassword() {
        when(userRepository.findByEmailIgnoreCase("asha@demo.com")).thenReturn(Optional.of(activeUser()));
        when(passwordEncoder.matches("wrong-pass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("asha@demo.com", "wrong-pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginFailsForDeactivatedUser() {
        User user = activeUser();
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("asha@demo.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("asha@demo.com", "secret123")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void registerInstituteRejectsDuplicateCode() {
        when(instituteRepository.existsByCodeIgnoreCase("DEMO")).thenReturn(true);
        RegisterInstituteRequest request = new RegisterInstituteRequest(
                "Demo Institute", "demo", "Admin", "admin@demo.com", "password123", null);

        assertThatThrownBy(() -> authService.registerInstitute(request))
                .isInstanceOf(ConflictException.class);
        verify(instituteRepository, never()).save(any());
    }
}
