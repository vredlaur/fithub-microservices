package com.fithub.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequest(
    @NotNull Long clientId,
    @NotNull Long fitnessClassId
) {
}
