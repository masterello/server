package com.masterello.auth.responsehandlers;

import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleFailureAuthHandler implements AuthenticationFailureHandler {

    private final CustomStatelessAuthorizationRequestRepository authorizationRequestRepository;
    @Value("${masterello.auth.google.success-redirect-url}")
    private String googleSuccessRedirectUrl;
    @Value("${masterello.auth.google.error-redirect-url}")
    private String googleFailureRedirectUrl;

    public GoogleFailureAuthHandler(CustomStatelessAuthorizationRequestRepository authorizationRequestRepository) {
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        OAuth2AuthorizationRequest authRequest = authorizationRequestRepository.removeAuthorizationRequest(request, response);

        String source = null;
        if (authRequest != null) {
            source = (String) authRequest.getAdditionalParameters().get("source");
        }
        response.sendRedirect(googleFailureRedirectUrl + "?error=google_auth_failed&someParam=" +  (source != null ? source : ""));
    }
}
