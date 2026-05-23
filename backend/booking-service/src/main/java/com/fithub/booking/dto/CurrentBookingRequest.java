package com.fithub.booking.dto;

import jakarta.validation.constraints.NotNull;

public record CurrentBookingRequest(
    @NotNull Long fitnessClassId
) {
}
