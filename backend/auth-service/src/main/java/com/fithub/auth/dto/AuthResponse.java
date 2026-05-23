package com.fithub.auth.dto;

import java.util.Set;

public record AuthResponse(
    String token,
    String username,
    String email,
    Set<String> roles
) {
}
