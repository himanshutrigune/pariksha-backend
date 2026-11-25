package com.pariksha.security;

import com.pariksha.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(AuthUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.email())
                .claim("uid", user.userId())
                .claim("role", user.role().name())
                .claim("iid", user.instituteId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public long expiresInSeconds() {
        return expirationMinutes * 60;
    }

    /** Returns the user if the token is valid and not expired, otherwise empty. */
    public Optional<AuthUser> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            Long userId = ((Number) claims.get("uid")).longValue();
            Long instituteId = ((Number) claims.get("iid")).longValue();
            Role role = Role.valueOf((String) claims.get("role"));
            return Optional.of(new AuthUser(userId, claims.getSubject(), role, instituteId));
        } catch (JwtException | IllegalArgumentException | NullPointerException | ClassCastException e) {
            return Optional.empty();
        }
    }
}
