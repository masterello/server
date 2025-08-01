package com.masterello.auth.customgrants.googlegrant;

import com.masterello.auth.customgrants.AbstractAuthenticationProvider;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.user.service.AuthNService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

import static com.masterello.auth.customgrants.googlegrant.GoogleAuthCodeAuthenticationConverter.GOOGLE_AUTH_CODE_GRANT_TYPE;
import static com.masterello.auth.service.JpaOAuth2AuthorizationService.AUTHORIZATION_CODE_TOKEN_TYPE;
import static com.masterello.auth.util.AuthenticationUtil.getAuthenticatedClientElseThrowInvalidClient;

@Component
@Slf4j
public class GoogleAuthCodeAuthenticationProvider extends AbstractAuthenticationProvider implements AuthenticationProvider {

    private final AuthNService authNService;

    @Autowired
    public GoogleAuthCodeAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                SecurityUserDetailsService userDetailsService,
                                                AuthNService authNService) {
        super(authorizationService, tokenGenerator, userDetailsService);
        this.authNService = authNService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GoogleAuthCodeAuthenticationToken googleAuthToken = (GoogleAuthCodeAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(authentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null ||
                !registeredClient.getAuthorizationGrantTypes().contains(googleAuthToken.getGrantType())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        OAuth2Authorization authorization = getAuthTokenAuthorization(authentication);

        val principal = getPrincipal(authorization);
        //-----------TOKEN BUILDERS----------
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = getTokenContextBuilder(authentication, registeredClient, principal);
        tokenContextBuilder.authorizationGrantType(googleAuthToken.getGrantType()); // TODO remove when switch to new grant

        OAuth2Authorization.Builder authorizationBuilder =  OAuth2Authorization.from(authorization);

        //-----------ACCESS TOKEN----------
        OAuth2AccessToken accessToken = getAccessToken(tokenContextBuilder, authorizationBuilder);

        //-----------REFRESH TOKEN----------
        OAuth2RefreshToken refreshToken = getRefreshToken(registeredClient, clientPrincipal, tokenContextBuilder, authorizationBuilder);

        OAuth2Authorization updatedAuthorization = authorizationBuilder.build();
        this.authorizationService.save(updatedAuthorization);

        Map<String, Object> additionalParameters = Map.of("roles", principal.getPrincipal().getRoles());

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    @Override
    protected MasterelloAuthenticationToken getPrincipal(Authentication authentication) {
        return null; // Not used
    }

    private OAuth2Authorization getAuthTokenAuthorization(Authentication authentication) {
        GoogleAuthCodeAuthenticationToken googleAuthToken = (GoogleAuthCodeAuthenticationToken) authentication;

        OAuth2Authorization authorization = this.authorizationService.findByToken(
                googleAuthToken.getToken(), AUTHORIZATION_CODE_TOKEN_TYPE);
        if (authorization == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        if (!authorizationCode.isActive()) {
            if (authorizationCode.isInvalidated()) {
                // Invalidate the access (and refresh) token as the client is attempting to use the authorization code more than once
                log.error("Google Authentication Code has been reused. Deleting all the associated tokens");
                authorizationService.remove(authorization);
            }
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        // Invalidate the authorization code as it can only be used once
        authorization = invalidate(authorization, authorizationCode.getToken());
        return authorization;
    }

    private OAuth2Authorization invalidate(OAuth2Authorization authorization, OAuth2AuthorizationCode token) {
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization)
                .token(token,
                        (metadata) ->
                                metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
        return authorizationBuilder.build();
    }

    private MasterelloAuthenticationToken getPrincipal(OAuth2Authorization authorization) {
        return authorization.getAttribute(Principal.class.getName());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GoogleAuthCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected AuthorizationGrantType getGrantType() {
        return new AuthorizationGrantType(GOOGLE_AUTH_CODE_GRANT_TYPE);
    }
}

