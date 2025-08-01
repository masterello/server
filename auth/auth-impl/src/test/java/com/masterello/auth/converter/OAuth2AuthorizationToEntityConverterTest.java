package com.masterello.auth.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.config.AuthorisationServerConfig;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.TokenPair;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;

import java.security.Principal;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthorizationToEntityConverterTest {

    private final Supplier<ObjectMapper> authServiceObjectMapper = new AuthorisationServerConfig().authServiceObjectMapper();

    private OAuth2AuthorizationToEntityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new OAuth2AuthorizationToEntityConverter(authServiceObjectMapper);
    }

    @SneakyThrows
    @Test
    void toTokenPairEntity_shouldConvertCorrectly() {
        // Given
        MasterelloTestUser user = getUser();
        user.setEmailVerified(false);
        user.setStatus(UserStatus.BANNED);
        user.setRoles(Set.of(Role.USER, Role.WORKER, Role.ADMIN));

        // When
        var authorization = getOAuthAuthorization(user);
        TokenPair actual = converter.toTokenPairEntity(authorization);

        // Then
        TokenPair expected = getTokenPair();

        assertNotNull(actual.getId());
        assertNotNull(actual.getIssuedAt());
        assertEquals(expected.getAccessTokenType(), actual.getAccessTokenType());
        assertEquals(expected.getAccessTokenValue(), actual.getAccessTokenValue());
        assertEquals(expected.getAccessTokenScopes(), actual.getAccessTokenScopes());
        assertEquals(expected.getAccessTokenExpiresAt(), actual.getAccessTokenExpiresAt());

        assertEquals(expected.getRefreshTokenValue(), actual.getRefreshTokenValue());
        assertEquals(expected.getRefreshTokenExpiresAt(), actual.getRefreshTokenExpiresAt());

        ObjectMapper mapper = authServiceObjectMapper.get();

        Map<String, Object> expectedAccessMeta = mapper.readValue(expected.getAccessTokenMetadata(), new TypeReference<>() {});
        Map<String, Object> actualAccessMeta = mapper.readValue(actual.getAccessTokenMetadata(), new TypeReference<>() {});
        assertEquals(expectedAccessMeta, actualAccessMeta);

        Map<String, Object> expectedRefreshMeta = mapper.readValue(expected.getRefreshTokenMetadata(), new TypeReference<>() {});
        Map<String, Object> actualRefreshMeta = mapper.readValue(actual.getRefreshTokenMetadata(), new TypeReference<>() {});
        assertEquals(expectedRefreshMeta, actualRefreshMeta);
    }

    @SneakyThrows
    @Test
    void toAuthorizationEntity_shouldConvertCorrectly() {
        // Given
        MasterelloTestUser user = getUser();
        user.setEmailVerified(false);
        user.setStatus(UserStatus.BANNED);
        user.setRoles(Set.of(Role.USER, Role.WORKER, Role.ADMIN));

        var authorization = getOAuthAuthorization(user);

        // When
        Authorization entity = converter.toAuthorizationEntity(authorization);

        // Then
        assertEquals(authorization.getId(), entity.getId());
        assertEquals(authorization.getAuthorizationGrantType().getValue(), entity.getAuthorizationGrantType());
        assertEquals(authorization.getPrincipalName(), entity.getPrincipalName());
        assertEquals(authorization.getRegisteredClientId(), entity.getRegisteredClientId());

        // Check principal extraction
        MasterelloAuthenticationToken principalToken =
                (MasterelloAuthenticationToken) authorization.getAttributes().get(Principal.class.getName());
        assertEquals(principalToken.getName(), entity.getPrincipal());

        // Check authorized scopes
        if (!authorization.getAuthorizedScopes().isEmpty()) {
            assertEquals(String.join(",", authorization.getAuthorizedScopes()), entity.getAuthorizedScopes());
        }

        // Check authorization code
        var authCode = authorization.getToken(OAuth2AuthorizationCode.class);
        if (authCode != null) {
            assertEquals(authCode.getToken().getTokenValue(), entity.getAuthorizationCodeValue());
            assertEquals(authCode.getToken().getIssuedAt().atOffset(ZoneOffset.UTC), entity.getAuthorizationCodeIssuedAt());
            assertEquals(authCode.getToken().getExpiresAt().atOffset(ZoneOffset.UTC), entity.getAuthorizationCodeExpiresAt());

            Map<String, Object> expectedMetadata = authCode.getMetadata();
            Map<String, Object> actualMetadata = authServiceObjectMapper.get()
                    .readValue(entity.getAuthorizationCodeMetadata(), new TypeReference<>() {});
            assertEquals(expectedMetadata, actualMetadata);
        }
    }
}
