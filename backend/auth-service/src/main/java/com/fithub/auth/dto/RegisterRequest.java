package com.fithub.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String username,
    @Email @NotBlank String email,
    @Size(min = 8, message = "Parola trebuie sa aiba minimum 8 caractere.") String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String phone
) {
}
