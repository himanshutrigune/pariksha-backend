package com.pariksha.security;

import com.pariksha.user.Role;

/**
 * The logged-in user, rebuilt from the JWT on every request.
 * Controllers receive it with @AuthenticationPrincipal.
 */
public record AuthUser(Long userId, String email, Role role, Long instituteId) {
}
