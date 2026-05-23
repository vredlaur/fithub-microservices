package com.fithub.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final int requestsPerMinute;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${app.rate-limit.requests-per-minute:120}") int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String key = request.getRemoteAddr();
        long minute = Instant.now().getEpochSecond() / 60;
        Bucket bucket = buckets.compute(key, (ignored, current) -> current == null || current.minute != minute
            ? new Bucket(minute)
            : current);
        if (bucket.count.incrementAndGet() > requestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":429,\"message\":\"Prea multe cereri. Incearca din nou peste un minut.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static class Bucket {
        private final long minute;
        private final AtomicInteger count = new AtomicInteger();

        private Bucket(long minute) {
            this.minute = minute;
        }
    }
}
