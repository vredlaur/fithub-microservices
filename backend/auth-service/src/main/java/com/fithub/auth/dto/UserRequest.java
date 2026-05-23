package com.fithub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserRequest(
    @NotBlank String username,
    @Email @NotBlank String email,
    @Size(min = 8, message = "Parola trebuie sa aiba minimum 8 caractere.") String password,
    boolean enabled,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String phone,
    Set<String> roles
) {
}
