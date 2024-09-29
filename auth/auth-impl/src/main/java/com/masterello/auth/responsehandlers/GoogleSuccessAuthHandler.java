package com.masterello.auth.responsehandlers;

import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class GoogleSuccessAuthHandler implements AuthenticationSuccessHandler {

    private final CustomStatelessAuthorizationRequestRepository authorizationRequestRepository;

    public GoogleSuccessAuthHandler(CustomStatelessAuthorizationRequestRepository authorizationRequestRepository) {
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken)
                authentication;
        authorizationRequestRepository.removeCookie(request, response);
        response.sendRedirect("/login/google?OID_TOKEN=" + ((OidcUser) oauth2Token.getPrincipal()).getIdToken().getTokenValue());
    }
}
