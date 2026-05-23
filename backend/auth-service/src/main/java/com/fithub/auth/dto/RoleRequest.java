package com.fithub.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(@NotBlank String name) {
}
