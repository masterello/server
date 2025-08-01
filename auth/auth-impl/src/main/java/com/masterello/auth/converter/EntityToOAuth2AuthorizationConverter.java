package com.masterello.auth.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.domain.TokenPair;
import com.masterello.auth.helper.UserClaimsHelper;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloUser;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
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

import static org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token.CLAIMS_METADATA_NAME;
import static org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token.INVALIDATED_METADATA_NAME;

@RequiredArgsConstructor
@Component
public class EntityToOAuth2AuthorizationConverter {

    private final Supplier<ObjectMapper> authServiceObjectMapper;
    private final RegisteredClientRepository registeredClientRepository;
    private final MasterelloUserService userRepository;

    public OAuth2Authorization toOAuth2Authorization(Authorization authorization, @Nullable TokenPair tokenPair) {
        RegisteredClient client = registeredClientRepository.findById(authorization.getRegisteredClientId());
        MasterelloAuthenticationToken principal = toPrincipal(authorization.getPrincipal());


        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(client)
                .id(authorization.getId())
                .attribute(Principal.class.getName(), principal)
                .principalName(authorization.getPrincipalName())
                .authorizationGrantType(new AuthorizationGrantType(authorization.getAuthorizationGrantType()));

        if (authorization.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode authorizationCode = getAuthorizationCodeToken(authorization);
            Map<String, Object> authorizationCodeTokenMetadata = getAuthorizationCodeTokenMetadata(authorization);

            builder.token(authorizationCode, md -> md.putAll(authorizationCodeTokenMetadata));
        }

        if (tokenPair != null) {
            OAuth2AccessToken accessToken = getAccessToken(tokenPair);
            Map<String, Object> accessTokenMetadata = getAccessTokenMetadata(tokenPair, principal.getPrincipal());
            OAuth2RefreshToken refreshToken = getRefreshToken(tokenPair);
            Map<String, Object> refreshTokenMetadata = getRefreshTokenMetadata(tokenPair);

            builder
                    .token(accessToken, md -> md.putAll(accessTokenMetadata))
                    .token(refreshToken, md -> md.putAll(refreshTokenMetadata));
        }

        return builder.build();
    }

    private OAuth2AuthorizationCode getAuthorizationCodeToken(Authorization authorization) {
        return new OAuth2AuthorizationCode(authorization.getAuthorizationCodeValue(),
                authorization.getAuthorizationCodeIssuedAt().toInstant(),
                authorization.getAuthorizationCodeExpiresAt().toInstant());
    }

    private Map<String, Object> getAuthorizationCodeTokenMetadata(Authorization authorization) {
        return new HashMap<>(parseObject(authorization.getAuthorizationCodeMetadata(), new TypeReference<>() {
        }));
    }

    private OAuth2RefreshToken getRefreshToken(TokenPair tokenPair) {
        return new OAuth2RefreshToken(tokenPair.getRefreshTokenValue(),
                tokenPair.getIssuedAt().toInstant(),
                tokenPair.getRefreshTokenExpiresAt().toInstant());
    }

    private Map<String, Object> getRefreshTokenMetadata(TokenPair tokenPair) {
        Map<String, Object> metadata = new HashMap<>(parseObject(tokenPair.getRefreshTokenMetadata(), new TypeReference<>() {
        }));

        if (tokenPair.isRevoked()) {
            metadata.put(INVALIDATED_METADATA_NAME, true);
        }
        return metadata;
    }

    private OAuth2AccessToken getAccessToken(TokenPair tokenPair) {
        Set<String> scopes = Set.of();
        if (StringUtils.hasText(tokenPair.getAuthorization().getAuthorizedScopes())) {
            scopes = Set.of(tokenPair.getAuthorization().getAuthorizedScopes().split(","));
        }
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                tokenPair.getAccessTokenValue(), tokenPair.getIssuedAt().toInstant(),
                tokenPair.getAccessTokenExpiresAt().toInstant(), scopes);
    }

    private Map<String, Object> getAccessTokenMetadata(TokenPair tokenPair, MasterelloUser user) {
        Map<String, Object> metadata = new HashMap<>(parseObject(tokenPair.getAccessTokenMetadata(), new TypeReference<>() {
        }));
        //noinspection unchecked
        Map<String, Object> claims = (Map<String, Object>) metadata.get(CLAIMS_METADATA_NAME);
        Map<String, Object> updatedClaims = new HashMap<>(claims);

        Map<String, Object> userClaims = UserClaimsHelper.getUserClaims(user);
        updatedClaims.putAll(userClaims);
        metadata.put(CLAIMS_METADATA_NAME, updatedClaims);
        if (tokenPair.isRevoked()) {
            metadata.put(INVALIDATED_METADATA_NAME, true);
        }
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