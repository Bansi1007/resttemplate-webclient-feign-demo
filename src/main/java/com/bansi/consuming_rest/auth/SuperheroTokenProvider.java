package com.bansi.consuming_rest.auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
public class SuperheroTokenProvider {

    @Value("${superhero.auth-url}")
    private String authUrl;

    @Value("${superhero.username}")
    private String username;

    @Value("${superhero.password}")
    private String password;

    private final RestTemplate authRestTemplate = new RestTemplate();

    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.MIN;

    public synchronized String getToken() {
        if (cachedToken == null || Instant.now().isAfter(expiresAt)) {
            refreshToken();
        }
        return cachedToken;
    }

    public synchronized void forceRefresh() {
        refreshToken();
    }

    private void refreshToken() {
        Map<String, String> body = Map.of("username", username, "password", password);
        Map<?, ?> response = authRestTemplate.postForObject(authUrl, body, Map.class);
        cachedToken = (String) response.get("token");
        expiresAt = Instant.now().plusSeconds(50 * 60); // stays under Superhero's 1-hour jwt.expiration
    }
}
