package com.fithub.gym;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fithub.gym.repository.FitnessClassRepository;
import java.time.LocalDateTime;
import java.util.Map;
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
class GymFlowIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    FitnessClassRepository fitnessClassRepository;

    @Test
    void adminCreatesGymDataAndFitnessClassWithPagination() throws Exception {
        long locationId = postAndReadId("/api/locations", Map.of(
            "name", "Integration Gym",
            "address", "Strada Test 1",
            "city", "Bucuresti",
            "active", true
        ));
        long roomId = postAndReadId("/api/rooms", Map.of(
            "location", Map.of("id", locationId),
            "name", "Sala Test",
            "capacity", 20,
            "active", true
        ));
        long trainerId = postAndReadId("/api/trainers", Map.of(
            "firstName", "Mara",
            "lastName", "Ionescu",
            "email", "mara.integration@fithub.local",
            "specialization", "HIIT",
            "active", true
        ));
        long classTypeId = postAndReadId("/api/class-types", Map.of(
            "name", "Integration HIIT",
            "description", "Test class type",
            "difficultyLevel", "MEDIUM"
        ));

        String start = LocalDateTime.now().plusDays(5).withNano(0).toString();
        String end = LocalDateTime.now().plusDays(5).plusHours(1).withNano(0).toString();
        long classId = postAndReadId("/api/classes", Map.of(
            "classType", Map.of("id", classTypeId),
            "trainer", Map.of("id", trainerId),
            "trainingRoom", Map.of("id", roomId),
            "name", "Integration Class",
            "startTime", start,
            "endTime", end,
            "capacity", 20,
            "availableSlots", 20,
            "status", "SCHEDULED"
        ));

        mockMvc.perform(get("/api/classes")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "name,asc")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").exists());

        assertThat(fitnessClassRepository.findById(classId)).isPresent();
    }

    private long postAndReadId(String path, Map<String, Object> body) throws Exception {
        String response = mockMvc.perform(post(path)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}
