package com.fithub.booking.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.dto.ClientProfileRequest;
import com.fithub.booking.entity.Client;
import com.fithub.booking.service.ClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {
    @Mock
    ClientService service;
    @Mock
    JwtService jwtService;

    @Test
    void meUsesUserIdFromJwt() {
        Client client = new Client();
        client.setId(7L);
        when(jwtService.userId("token")).thenReturn(42L);
        when(service.findByAuthUserId(42L)).thenReturn(client);

        ClientController controller = new ClientController(service, jwtService);

        assertThat(controller.me("Bearer token").getId()).isEqualTo(7L);
        verify(service).findByAuthUserId(42L);
    }

    @Test
    void upsertMeUsesUserIdFromJwt() {
        Client client = new Client();
        client.setId(8L);
        ClientProfileRequest request = new ClientProfileRequest("User", "Nou", "usernou@example.com", "0700000001");
        when(jwtService.userId("token")).thenReturn(42L);
        when(service.upsertCurrentClient(42L, request)).thenReturn(client);

        ClientController controller = new ClientController(service, jwtService);

        assertThat(controller.upsertMe("Bearer token", request).getId()).isEqualTo(8L);
        verify(service).upsertCurrentClient(42L, request);
    }
}
