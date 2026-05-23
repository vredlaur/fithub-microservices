package com.fithub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fithub.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;

    @Test
    void registerThenLoginReturnsJwtWithUserIdAndRoles() throws Exception {
        String username = "integration_user";
        String email = "integration_user@fithub.local";
        userRepository.findByUsername(username).ifPresent(userRepository::delete);

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "integration_user",
                      "email": "integration_user@fithub.local",
                      "password": "Password1",
                      "firstName": "Integration",
                      "lastName": "User",
                      "phone": "0700000000"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.userId").isNumber())
            .andExpect(jsonPath("$.roles[0]").value("USER"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode registered = objectMapper.readTree(registerResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "integration_user",
                      "password": "Password1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(registered.get("userId").asLong()))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.token").isString());

        assertThat(userRepository.findByUsername(username)).isPresent();
    }
}
