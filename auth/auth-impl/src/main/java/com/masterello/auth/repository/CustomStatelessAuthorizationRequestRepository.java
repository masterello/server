package com.masterello.auth.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.domain.AuthorizationRequestEntity;
import com.masterello.auth.service.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomStatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Duration OAUTH_REQUEST_EXPIRY = Duration.ofMinutes(5);
    public static final String CACHED_AUTH_REQUEST_ATTRIBUTE = "cached_oauth2_auth_request";

    private final EncryptionService encryptionService;
    private final AuthorizationRequestEntityRepository entityRepository;
    private final ObjectMapper objectMapper = getObjectMapper();

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OAuth2ClientJackson2Module());
        objectMapper.registerModule(new CoreJackson2Module());
        return objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null) return null;

        OAuth2AuthorizationRequest authRequest = entityRepository.findById(state)
                .map(entity -> deserialize(entity.getRequestJson()))
                .orElse(null);
        if (authRequest != null) {
            request.setAttribute(CACHED_AUTH_REQUEST_ATTRIBUTE, authRequest);
        }
        return authRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) return;

        String state = authorizationRequest.getState();
        String json = serialize(authorizationRequest);

        AuthorizationRequestEntity entity = new AuthorizationRequestEntity(
                state,
                json,
                Instant.now().plus(OAUTH_REQUEST_EXPIRY)
        );

        entityRepository.save(entity);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        if (state == null) return null;

        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        entityRepository.deleteById(state);
        return authRequest;
    }

    @SneakyThrows
    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        String json = objectMapper.writer().writeValueAsString(authorizationRequest);
        return encryptionService.encrypt(json);
    }

    @SneakyThrows
    private OAuth2AuthorizationRequest deserialize(String encrypted) {
        String decrypted = encryptionService.decrypt(encrypted);
        return objectMapper.reader().readValue(decrypted.getBytes(StandardCharsets.UTF_8), OAuth2AuthorizationRequest.class);
    }

}