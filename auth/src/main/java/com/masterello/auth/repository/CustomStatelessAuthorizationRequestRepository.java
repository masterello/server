package com.masterello.auth.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.helper.CookieHelper;
import com.masterello.auth.service.EncryptionService;
import jakarta.servlet.http.Cookie;
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

@Component
@RequiredArgsConstructor
public class CustomStatelessAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Duration OAUTH_COOKIE_EXPIRY = Duration.ofMinutes(5);
    private static final String OAUTH_COOKIE_NAME = "OAUTH";
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper = getObjectMapper();

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OAuth2ClientJackson2Module());
        objectMapper.registerModule(new CoreJackson2Module());
        return objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return this.retrieveCookie(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            this.removeCookie(request, response);
            return;
        }
        this.attachCookie(request, response, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.retrieveCookie(request);
    }

    private OAuth2AuthorizationRequest retrieveCookie(HttpServletRequest request) {
        return CookieHelper.retrieve(request.getCookies(), OAUTH_COOKIE_NAME)
                .map(this::decrypt)
                .orElse(null);
    }

    private void attachCookie(HttpServletRequest request, HttpServletResponse response, OAuth2AuthorizationRequest value) {
        Cookie cookie = CookieHelper.generateCookie(request, OAUTH_COOKIE_NAME, this.encrypt(value), OAUTH_COOKIE_EXPIRY);
        response.addCookie(cookie);
    }

    public void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie expiredCookie = CookieHelper.generateExpiredCookie(request, OAUTH_COOKIE_NAME);
        response.addCookie(expiredCookie);
    }

    @SneakyThrows
    private String encrypt(OAuth2AuthorizationRequest authorizationRequest) {
        String json = objectMapper.writer().writeValueAsString(authorizationRequest);
        return encryptionService.encrypt(json);
    }

    @SneakyThrows
    private OAuth2AuthorizationRequest decrypt(String encrypted) {
        String decrypted = encryptionService.decrypt(encrypted);
        return objectMapper.reader().readValue(decrypted.getBytes(StandardCharsets.UTF_8), OAuth2AuthorizationRequest.class);
    }

}