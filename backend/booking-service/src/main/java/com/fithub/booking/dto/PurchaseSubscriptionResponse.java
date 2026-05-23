package com.fithub.booking.dto;

import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.entity.Payment;

public record PurchaseSubscriptionResponse(
    ClientSubscription subscription,
    Payment payment
) {
}
