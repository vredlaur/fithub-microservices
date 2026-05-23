package com.fithub.booking.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "gym-service")
public interface GymClient {
    @GetMapping("/api/classes/{id}/availability")
    Map<String, Object> availability(@PathVariable Long id);

    @PostMapping("/api/classes/{id}/reserve-slot")
    void reserveSlot(@PathVariable Long id);

    @PostMapping("/api/classes/{id}/release-slot")
    void releaseSlot(@PathVariable Long id);
}
