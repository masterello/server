package com.masterello.auth.customgrants;


import com.masterello.auth.service.SecurityUserDetailsService;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.Map;

public abstract class AbstractAuthenticationProvider implements AuthenticationProvider  {

    private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
    private final OAuth2AuthorizationService authorizationService;
    protected final SecurityUserDetailsService userDetailsService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public AbstractAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                          OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                          SecurityUserDetailsService userDetailsService) {

        Assert.notNull(authorizationService, "authorizationService cannot be null");
        Assert.notNull(tokenGenerator, "TokenGenerator cannot be null");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(authentication);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null || !registeredClient.getAuthorizationGrantTypes().contains(getGrantType())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        val principal = getPrincipal(authentication);
        //-----------TOKEN BUILDERS----------
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = getTokenContextBuilder(authentication, registeredClient, principal);
        OAuth2Authorization.Builder authorizationBuilder = getAuthorizationBuilder(registeredClient, principal);

        //-----------ACCESS TOKEN----------
        OAuth2AccessToken accessToken = getAccessToken(tokenContextBuilder, authorizationBuilder);

        //-----------REFRESH TOKEN----------
        OAuth2RefreshToken refreshToken = getRefreshToken(registeredClient, clientPrincipal, tokenContextBuilder, authorizationBuilder);

        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        Map<String, Object> additionalParameters = Map.of("roles", principal.getPrincipal().getRoles());

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    private OAuth2RefreshToken getRefreshToken(RegisteredClient registeredClient, OAuth2ClientAuthenticationToken clientPrincipal, DefaultOAuth2TokenContext.Builder tokenContextBuilder, OAuth2Authorization.Builder authorizationBuilder) {
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN) &&
                !clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

            OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
            if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                        "The token generator failed to generate the refresh token.", ERROR_URI);
                throw new OAuth2AuthenticationException(error);
            }
            refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
            authorizationBuilder.refreshToken(refreshToken);
        }
        return refreshToken;
    }

    private OAuth2AccessToken getAccessToken(DefaultOAuth2TokenContext.Builder tokenContextBuilder, OAuth2Authorization.Builder authorizationBuilder) {
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", ERROR_URI);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        if (generatedAccessToken instanceof ClaimAccessor) {
            authorizationBuilder.token(accessToken, (metadata) ->
                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
        } else {
            authorizationBuilder.accessToken(accessToken);
        }
        return accessToken;
    }

    private OAuth2Authorization.Builder getAuthorizationBuilder(RegisteredClient registeredClient, Authentication principal) {
        return OAuth2Authorization.withRegisteredClient(registeredClient)
                .attribute(Principal.class.getName(), principal)
                .principalName(principal.getName())
                .authorizationGrantType(getGrantType());
    }

    private DefaultOAuth2TokenContext.Builder getTokenContextBuilder(Authentication authenticationToken, RegisteredClient registeredClient, Authentication principal) {
        return DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(getGrantType())
                .authorizationGrant(authenticationToken);
    }

    private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    protected abstract MasterelloAuthenticationToken getPrincipal(Authentication authentication);

    protected abstract AuthorizationGrantType getGrantType();
}
