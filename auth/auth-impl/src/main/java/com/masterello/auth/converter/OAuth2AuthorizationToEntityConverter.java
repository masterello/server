package com.masterello.auth.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class OAuth2AuthorizationToEntityConverter {

    private final Supplier<ObjectMapper> authServiceObjectMapper;

    public Authorization toAuthorizationEntity(OAuth2Authorization authorization) {
        Authorization authEntity = new Authorization();
        authEntity.setId(authorization.getId());
        if (!authorization.getAuthorizedScopes().isEmpty()) {
            authEntity.setAuthorizedScopes(String.join(",", authorization.getAuthorizedScopes()));
        }
        authEntity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
        authEntity.setPrincipalName(authorization.getPrincipalName());
        authEntity.setPrincipal(getPrincipal(authorization.getAttributes().get(Principal.class.getName())));
        authEntity.setRegisteredClientId(authorization.getRegisteredClientId());

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
        if (authorizationCode != null) {
            authEntity.setAuthorizationCodeIssuedAt(Objects.requireNonNull(authorizationCode.getToken().getIssuedAt()).atOffset(ZoneOffset.UTC));
            authEntity.setAuthorizationCodeExpiresAt(Objects.requireNonNull(authorizationCode.getToken().getExpiresAt()).atOffset(ZoneOffset.UTC));
            authEntity.setAuthorizationCodeValue(authorizationCode.getToken().getTokenValue());
            authEntity.setAuthorizationCodeMetadata(writeObject(authorizationCode.getMetadata()));
        }

        return authEntity;
    }

    public TokenPair toTokenPairEntity(OAuth2Authorization authorization) {
        TokenPair tokenPair = new TokenPair();
        tokenPair.setId(UUID.randomUUID());
        tokenPair.setIssuedAt(OffsetDateTime.now());
        var accessToken = authorization.getAccessToken();
        if (accessToken != null && accessToken.getToken() != null) {
            OAuth2AccessToken token = accessToken.getToken();
            tokenPair.setAccessTokenType(token.getTokenType().getValue());
            tokenPair.setAccessTokenValue(token.getTokenValue());
            tokenPair.setAccessTokenExpiresAt(Objects.requireNonNull(token.getExpiresAt()).atOffset(ZoneOffset.UTC));
            if (!token.getScopes().isEmpty()) {
                tokenPair.setAccessTokenScopes(String.join(",", token.getScopes()));
            }
            tokenPair.setAccessTokenMetadata(writeObject(accessToken.getMetadata()));
        }
        var refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.getToken() != null) {
            OAuth2RefreshToken token = refreshToken.getToken();
            tokenPair.setRefreshTokenValue(token.getTokenValue());
            tokenPair.setRefreshTokenExpiresAt(Objects.requireNonNull(token.getExpiresAt()).atOffset(ZoneOffset.UTC));
            tokenPair.setRefreshTokenMetadata(writeObject(refreshToken.getMetadata()));
        }
        return tokenPair;
    }

    private String getPrincipal(Object principal) {
        MasterelloAuthenticationToken token = (MasterelloAuthenticationToken) principal;
        return token.getName();
    }

    private String writeObject(Object data) {
        try {
            return authServiceObjectMapper.get().writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}