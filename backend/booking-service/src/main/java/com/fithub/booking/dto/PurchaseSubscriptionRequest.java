package com.fithub.booking.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseSubscriptionRequest(
    @NotNull Long subscriptionTypeId,
    String paymentMethod
) {
}
