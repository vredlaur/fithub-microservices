package com.fithub.booking.controller;

import com.fithub.booking.config.JwtService;
import com.fithub.booking.dto.BookingRequest;
import com.fithub.booking.dto.CurrentBookingRequest;
import com.fithub.booking.entity.Booking;
import com.fithub.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    private final JwtService jwtService;

    public BookingController(BookingService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping
    public Page<Booking> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Booking findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/me")
    public Page<Booking> mine(@RequestHeader("Authorization") String authorization, Pageable pageable) {
        return service.findByAuthUserId(currentUserId(authorization), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody BookingRequest request) {
        return service.create(request);
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createForCurrentUser(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody CurrentBookingRequest request
    ) {
        return service.createForAuthUser(currentUserId(authorization), request.fitnessClassId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteForCurrentUser(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        service.deleteForAuthUser(currentUserId(authorization), id);
    }

    private Long currentUserId(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
            ? authorization.substring(7)
            : authorization;
        return jwtService.userId(token);
    }
}
