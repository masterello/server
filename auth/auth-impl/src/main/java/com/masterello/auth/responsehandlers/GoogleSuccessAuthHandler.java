package com.masterello.auth.responsehandlers;

import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleSuccessAuthHandler implements AuthenticationSuccessHandler {

    private final CustomStatelessAuthorizationRequestRepository authorizationRequestRepository;
    @Value("${masterello.auth.google.success-redirect-url}")
    private String googleSuccessRedirectUrl;

    public GoogleSuccessAuthHandler(CustomStatelessAuthorizationRequestRepository authorizationRequestRepository) {
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthorizationRequest authRequest =
                (OAuth2AuthorizationRequest) request.getAttribute(CustomStatelessAuthorizationRequestRepository.CACHED_AUTH_REQUEST_ATTRIBUTE);

        String source = null;
        if (authRequest != null) {
            source = (String) authRequest.getAdditionalParameters().get("source");
        }

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken)
                authentication;
        response.sendRedirect(googleSuccessRedirectUrl +
                "?OID_TOKEN=" + ((OidcUser) oauth2Token.getPrincipal()).getIdToken().getTokenValue() +
                "&source=" + (source != null ? source : ""));
    }
}
