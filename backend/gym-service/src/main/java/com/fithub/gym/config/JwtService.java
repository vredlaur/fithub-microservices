package com.fithub.gym.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final ObjectMapper objectMapper;
    private final String secret;

    public JwtService(ObjectMapper objectMapper, @Value("${app.jwt.secret}") String secret) {
        this.objectMapper = objectMapper;
        this.secret = secret;
    }

    public boolean isValid(String token) {
        try {
            Map<String, Object> payload = payload(token);
            return ((Number) payload.get("exp")).longValue() > Instant.now().getEpochSecond();
        } catch (Exception exception) {
            return false;
        }
    }

    public String username(String token) {
        return (String) payload(token).get("sub");
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(String token) {
        Object roles = payload(token).get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    private Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            String unsigned = parts[0] + "." + parts[1];
            if (parts.length != 3 || !sign(unsigned).equals(parts[2])) {
                throw new IllegalArgumentException("Token JWT invalid.");
            }
            return objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token JWT invalid.", exception);
        }
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
