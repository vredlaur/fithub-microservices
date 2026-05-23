package com.fithub.auth.dto;

import java.util.Set;

public record AuthResponse(
    Long userId,
    String token,
    String username,
    String email,
    Set<String> roles
) {
}
