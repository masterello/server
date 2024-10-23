package com.masterello.auth.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
 import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class OAuth2AuthorizationToAuthEntityConverter {

    private final Supplier<ObjectMapper> authServiceObjectMapper;

    public Authorization toEntity(OAuth2Authorization authorization) {
        Authorization entity = new Authorization();
        entity.setId(authorization.getId());
        if (!authorization.getAuthorizedScopes().isEmpty()) {
            entity.setAuthorizedScopes(String.join(",", authorization.getAuthorizedScopes()));
        }
        entity.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
        entity.setPrincipalName(authorization.getPrincipalName());
        entity.setPrincipal(getPrincipal(authorization.getAttributes().get(Principal.class.getName())));
        entity.setRegisteredClientId(authorization.getRegisteredClientId());
        setTokens(entity, authorization);
        return entity;
    }

    private void setTokens(Authorization entity, OAuth2Authorization authorization) {
        var accessToken = authorization.getAccessToken();
        if (accessToken != null && accessToken.getToken() != null) {
            OAuth2AccessToken token = accessToken.getToken();
            entity.setAccessTokenType(token.getTokenType().getValue());
            entity.setAccessTokenValue(token.getTokenValue());
            entity.setAccessTokenIssuedAt(Objects.requireNonNull(token.getIssuedAt()).atOffset(ZoneOffset.UTC));
            entity.setAccessTokenExpiresAt(Objects.requireNonNull(token.getExpiresAt()).atOffset(ZoneOffset.UTC));
            if (!token.getScopes().isEmpty()) {
                entity.setAccessTokenScopes(String.join(",", token.getScopes()));
            }
            entity.setAccessTokenMetadata(writeObject(accessToken.getMetadata()));
        }
        var refreshToken = authorization.getRefreshToken();
        if (refreshToken != null && refreshToken.getToken() != null) {
            OAuth2RefreshToken token = refreshToken.getToken();
            entity.setRefreshTokenValue(token.getTokenValue());
            entity.setRefreshTokenIssuedAt(Objects.requireNonNull(token.getIssuedAt()).atOffset(ZoneOffset.UTC));
            entity.setRefreshTokenExpiresAt(Objects.requireNonNull(token.getExpiresAt()).atOffset(ZoneOffset.UTC));

            entity.setRefreshTokenMetadata(writeObject(refreshToken.getMetadata()));
        }
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