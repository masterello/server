package com.masterello.auth.responsehandlers;

import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import com.masterello.auth.service.CustomAuthorizationCodeService;
import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.user.service.AuthNService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleSuccessAuthHandler implements AuthenticationSuccessHandler {

    private final SecurityUserDetailsService userDetailsService;
    private final CustomAuthorizationCodeService customAuthorizationCodeService;
    private final AuthNService authNService;

    @Value("${masterello.auth.google.success-redirect-url}")
    private String googleSuccessRedirectUrl;


    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthorizationRequest authRequest =
                (OAuth2AuthorizationRequest) request.getAttribute(CustomStatelessAuthorizationRequestRepository.CACHED_AUTH_REQUEST_ATTRIBUTE);

        String source = null;
        if (authRequest != null) {
            source = (String) authRequest.getAdditionalParameters().get("source");
        }

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken)
                authentication;
        val userInfo = oauth2Token.getPrincipal();
        String email = userInfo.getAttribute("email");
        String name = userInfo.getAttribute("given_name");
        String lastname = userInfo.getAttribute("family_name");
        SecurityUserDetails user = fetchOrCreateUser(email, name, lastname);
        String authorizationCode = customAuthorizationCodeService.generateAuthorizationCode(new MasterelloAuthenticationToken(user));
        response.sendRedirect(googleSuccessRedirectUrl +
                "?AUTH_CODE=" + authorizationCode +
                "&OID_TOKEN=" + authorizationCode + // TODO Remove when migrated to AUTH_CODE
                "&source=" + (source != null ? source : ""));
    }

    private SecurityUserDetails fetchOrCreateUser(String email, String name, String lastname) {
        if (userDetailsService.existsByEmail(email)) {
            return userDetailsService.loadUserByUsername(email);
        } else {
            return new SecurityUserDetails(authNService.googleSignup(email, name, lastname));
        }
    }
}
