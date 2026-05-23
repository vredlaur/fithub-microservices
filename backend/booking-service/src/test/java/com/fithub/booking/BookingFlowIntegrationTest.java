package com.fithub.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fithub.booking.client.GymClient;
import com.fithub.booking.entity.Client;
import com.fithub.booking.entity.ClientSubscription;
import com.fithub.booking.entity.SubscriptionType;
import com.fithub.booking.repository.BookingRepository;
import com.fithub.booking.repository.ClientRepository;
import com.fithub.booking.repository.ClientSubscriptionRepository;
import com.fithub.booking.repository.NotificationRepository;
import com.fithub.booking.repository.SubscriptionTypeRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    SubscriptionTypeRepository subscriptionTypeRepository;
    @Autowired
    ClientSubscriptionRepository subscriptionRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @MockitoBean
    GymClient gymClient;

    @Test
    void clientWithActiveSubscriptionCreatesBookingAndNotification() throws Exception {
        Client client = clientRepository.save(client());
        SubscriptionType type = subscriptionTypeRepository.save(subscriptionType());
        ClientSubscription subscription = new ClientSubscription();
        subscription.setClient(client);
        subscription.setSubscriptionType(type);
        subscription.setStartDate(LocalDate.now().minusDays(1));
        subscription.setEndDate(LocalDate.now().plusDays(30));
        subscription.setStatus("ACTIVE");
        subscriptionRepository.save(subscription);
        when(gymClient.availability(77L)).thenReturn(Map.of("available", true));

        mockMvc.perform(post("/api/bookings/me")
                .header("Authorization", "Bearer " + tokenFor(client.getAuthUserId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fitnessClassId": 77
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fitnessClassId").value(77))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(gymClient).reserveSlot(77L);
        assertThat(bookingRepository.findAll()).anyMatch(booking -> booking.getFitnessClassId().equals(77L));
        assertThat(notificationRepository.findAll()).anyMatch(notification -> notification.getTitle().equals("Rezervare confirmata"));
    }

    private Client client() {
        Client client = new Client();
        client.setAuthUserId(555L);
        client.setFirstName("Integration");
        client.setLastName("Client");
        client.setEmail("integration.client@fithub.local");
        client.setPhone("0700000000");
        return client;
    }

    private SubscriptionType subscriptionType() {
        SubscriptionType type = new SubscriptionType();
        type.setName("Integration Standard");
        type.setDescription("Test subscription");
        type.setDurationDays(30);
        type.setPrice(BigDecimal.valueOf(100));
        type.setActive(true);
        return type;
    }

    private String tokenFor(Long userId) throws Exception {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("""
            {"sub":"user","userId":%d,"roles":["USER"],"exp":%d}
            """.formatted(userId, Instant.now().plusSeconds(3600).getEpochSecond()));
        String unsigned = header + "." + payload;
        return unsigned + "." + sign(unsigned);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("fithub-local-secret-key-change-me".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
