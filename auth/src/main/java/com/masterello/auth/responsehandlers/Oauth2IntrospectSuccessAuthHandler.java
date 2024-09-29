package com.masterello.auth.responsehandlers;

import com.masterello.auth.config.TokenProperties;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.repository.AuthorizationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenIntrospection;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenIntrospectionAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.http.converter.OAuth2TokenIntrospectionHttpMessageConverter;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenIntrospectionEndpointFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Oauth2IntrospectSuccessAuthHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final HttpMessageConverter<OAuth2TokenIntrospection> tokenIntrospectionHttpResponseConverter =
            new OAuth2TokenIntrospectionHttpMessageConverter();
    private final AuthorizationRepository authorizationRepository;
    private final TokenProperties tokenProperties;

    /**
     * Copy of the AuthenticationSuccessFilter defined in {@link OAuth2TokenIntrospectionEndpointFilter} except it also updates expiration for tokens
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication =
                (OAuth2TokenIntrospectionAuthenticationToken) authentication;
        OAuth2TokenIntrospection tokenClaims = refreshExpirationsAndGetUpdatedClaims(tokenIntrospectionAuthentication);
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        tokenIntrospectionHttpResponseConverter.write(tokenClaims, null, httpResponse);
    }

    private OAuth2TokenIntrospection refreshExpirationsAndGetUpdatedClaims(OAuth2TokenIntrospectionAuthenticationToken tokenIntrospectionAuthentication) {
        String token = tokenIntrospectionAuthentication.getToken();

        Optional<Authorization> optAuth = authorizationRepository.findByAccessTokenValue(token);
        if(optAuth.isEmpty()) {
            return tokenIntrospectionAuthentication.getTokenClaims();
        }

        Authorization auth = optAuth.get();
        OffsetDateTime accessTokenExpiresAt = Instant.now().plus(tokenProperties.getAccessTokenTtl())
                .atOffset(auth.getAccessTokenExpiresAt().getOffset());
        auth.setAccessTokenExpiresAt(accessTokenExpiresAt);

        auth.setRefreshTokenExpiresAt(Instant.now().plus(tokenProperties.getRefreshTokenTtl())
                .atOffset(auth.getRefreshTokenExpiresAt().getOffset()));

        authorizationRepository.save(auth);

        Map<String, Object> existingClaims = tokenIntrospectionAuthentication.getTokenClaims().getClaims();
        return OAuth2TokenIntrospection.withClaims(existingClaims)
                .expiresAt(accessTokenExpiresAt.toInstant()).build();

    }
}
