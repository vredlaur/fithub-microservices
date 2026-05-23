package com.fithub.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fithub.auth.entity.User;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMinutes;

    public JwtService(
        ObjectMapper objectMapper,
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-minutes:240}") long expirationMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getUsername());
        payload.put("roles", user.getRoles().stream().map(role -> role.getName()).toList());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond());
        return encode(Map.of("alg", "HS256", "typ", "JWT"), payload);
    }

    public boolean isValid(String token) {
        try {
            Map<String, Object> payload = payload(token);
            long exp = ((Number) payload.get("exp")).longValue();
            return exp > Instant.now().getEpochSecond();
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

    private String encode(Map<String, Object> header, Map<String, Object> payload) {
        try {
            String unsigned = base64Json(header) + "." + base64Json(payload);
            return unsigned + "." + sign(unsigned);
        } catch (Exception exception) {
            throw new IllegalStateException("Nu s-a putut genera tokenul JWT.", exception);
        }
    }

    private Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token JWT invalid.");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!sign(unsigned).equals(parts[2])) {
                throw new IllegalArgumentException("Semnatura JWT invalida.");
            }
            byte[] json = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token JWT invalid.", exception);
        }
    }

    private String base64Json(Map<String, Object> data) throws Exception {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(objectMapper.writeValueAsBytes(data));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
