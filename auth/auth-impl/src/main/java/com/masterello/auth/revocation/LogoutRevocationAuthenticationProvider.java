package com.masterello.auth.revocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.masterello.auth.util.AuthenticationUtil.getAuthenticatedClientElseThrowInvalidClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogoutRevocationAuthenticationProvider implements AuthenticationProvider {

    private final OAuth2AuthorizationService authorizationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthentication =
                (OAuth2TokenRevocationAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(tokenRevocationAuthentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        OAuth2TokenType auth2TokenType = Optional.ofNullable(tokenRevocationAuthentication.getTokenTypeHint())
                .filter(OAuth2TokenType.ACCESS_TOKEN.getValue()::equalsIgnoreCase)
                .map(hint -> OAuth2TokenType.ACCESS_TOKEN)
                .orElse(null);

        OAuth2Authorization authorization = this.authorizationService.findByToken(
                tokenRevocationAuthentication.getToken(), auth2TokenType);
        if (authorization == null) {
            if (log.isTraceEnabled()) {
                log.trace("Did not authenticate token revocation request since token was not found");
            }
            // Return the authentication request when token not found
            return tokenRevocationAuthentication;
        }

        if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }

        OAuth2Authorization.Token<OAuth2Token> token = authorization.getToken(tokenRevocationAuthentication.getToken());
        this.authorizationService.remove(authorization);

        log.trace("Revoked tokens");
        return new OAuth2TokenRevocationAuthenticationToken(token.getToken(), clientPrincipal);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2TokenRevocationAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
