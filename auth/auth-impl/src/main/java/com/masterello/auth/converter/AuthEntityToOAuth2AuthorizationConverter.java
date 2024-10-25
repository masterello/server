package com.masterello.auth.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.helper.UserClaimsHelper;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class AuthEntityToOAuth2AuthorizationConverter {

    private final Supplier<ObjectMapper> authServiceObjectMapper;
    private final RegisteredClientRepository registeredClientRepository;
    private final MasterelloUserService userRepository;

    public OAuth2Authorization toOAuth2Authorization(Authorization authorization) {
        RegisteredClient client = registeredClientRepository.findById(authorization.getRegisteredClientId());
        MasterelloAuthenticationToken principal = toPrincipal(authorization.getPrincipal());

        OAuth2AccessToken accessToken = getAccessToken(authorization);
        Map<String, Object> accessTokenMetadata = getAccessTokenMetadata(authorization, principal.getPrincipal());
        OAuth2RefreshToken refreshToken = getRefreshToken(authorization);
        Map<String, Object> refreshTokenMetadata = getRefreshTokenMetadata(authorization);

        return OAuth2Authorization.withRegisteredClient(client)
                .id(authorization.getId())
                .attribute(Principal.class.getName(), principal)
                .principalName(authorization.getPrincipalName())
                .authorizationGrantType(new AuthorizationGrantType(authorization.getAuthorizationGrantType()))
                .token(accessToken, md -> md.putAll(accessTokenMetadata))
                .token(refreshToken, md -> md.putAll(refreshTokenMetadata))
                .build();
    }

    private OAuth2RefreshToken getRefreshToken(Authorization authorization) {
        return new OAuth2RefreshToken(authorization.getRefreshTokenValue(),
                authorization.getRefreshTokenIssuedAt().toInstant(),
                authorization.getRefreshTokenExpiresAt().toInstant());
    }

    private Map<String, Object> getRefreshTokenMetadata(Authorization authorization) {
        return parseObject(authorization.getRefreshTokenMetadata(), new TypeReference<>() {
        });
    }

    private OAuth2AccessToken getAccessToken(Authorization authorization) {
        Set<String> scopes = Set.of();
        if (StringUtils.hasText(authorization.getAuthorizedScopes())) {
            scopes = Set.of(authorization.getAuthorizedScopes().split(","));
        }
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                authorization.getAccessTokenValue(), authorization.getAccessTokenIssuedAt().toInstant(),
                authorization.getAccessTokenExpiresAt().toInstant(), scopes);
    }

    private Map<String, Object> getAccessTokenMetadata(Authorization authorization, MasterelloUser user) {
        Map<String, Object> metadata = new HashMap<>(parseObject(authorization.getAccessTokenMetadata(), new TypeReference<>() {
        }));
        //noinspection unchecked
        Map<String, Object> claims = (Map<String, Object>) metadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME);
        Map<String, Object> updatedClaims = new HashMap<>(claims);

        Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);
        updatedClaims.putAll(userClaims);
        metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, updatedClaims);
        return metadata;
    }

    private MasterelloAuthenticationToken toPrincipal(String principal) {
        MasterelloUser user = userRepository.findById(UUID.fromString(principal))
                .orElseThrow(() -> new IllegalArgumentException("Deserialization of principal failed"));
        return new MasterelloAuthenticationToken(new SecurityUserDetails(user));
    }

    private <T> T parseObject(String object, TypeReference<T> type) {
        try {
            return authServiceObjectMapper.get().readValue(object, type);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}