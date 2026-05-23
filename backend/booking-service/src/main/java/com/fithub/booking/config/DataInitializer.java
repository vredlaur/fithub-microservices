package com.fithub.booking.config;

import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.entity.Payment;
import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.PaymentRepository;
import com.fithub.booking.repository.SubscriptionTypeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedBookingData(
        ClientRepository clients,
        SubscriptionTypeRepository subscriptionTypes,
        ClientSubscriptionRepository subscriptions,
        PaymentRepository payments
    ) {
        return args -> {
            if (clients.count() > 0) {
                return;
            }

            Client client = new Client();
            client.setAuthUserId(2L);
            client.setFirstName("User");
            client.setLastName("FitHub");
            client.setEmail("user@fithub.local");
            client.setPhone("0700000001");
            client = clients.save(client);

            SubscriptionType type = new SubscriptionType();
            type.setName("Standard");
            type.setDescription("Acces la clase fitness si sala.");
            type.setDurationDays(30);
            type.setPrice(BigDecimal.valueOf(149.99));
            type = subscriptionTypes.save(type);

            ClientSubscription subscription = new ClientSubscription();
            subscription.setClient(client);
            subscription.setSubscriptionType(type);
            subscription.setStartDate(LocalDate.now().minusDays(1));
            subscription.setEndDate(LocalDate.now().plusDays(29));
            subscription.setStatus("ACTIVE");
            subscription = subscriptions.save(subscription);

            Payment payment = new Payment();
            payment.setClient(client);
            payment.setClientSubscription(subscription);
            payment.setAmount(type.getPrice());
            payment.setStatus("PAID");
            payment.setMethod("CARD");
            payments.save(payment);
        };
    }
}
