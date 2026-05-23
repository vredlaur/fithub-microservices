package com.fithub.auth.exception;

import java.time.LocalDateTime;

public record ApiError(int status, String message, LocalDateTime timestamp) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, LocalDateTime.now());
    }
}
