package com.pariksha;

import com.pariksha.security.AuthUser;
import com.pariksha.security.JwtService;
import com.pariksha.user.Role;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    private final JwtService jwtService = new JwtService(SECRET, 60);

    @Test
    void tokenRoundTripKeepsUserDetails() {
        AuthUser user = new AuthUser(7L, "teacher@demo.com", Role.TEACHER, 3L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.parse(token)).contains(user);
    }

    @Test
    void garbageTokenIsRejected() {
        assertThat(jwtService.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder().encodeToString("ffffffffffffffffffffffffffffffff".getBytes());
        String token = new JwtService(otherSecret, 60).generateToken(new AuthUser(1L, "a@b.com", Role.ADMIN, 1L));

        assertThat(jwtService.parse(token)).isEmpty();
    }
}
