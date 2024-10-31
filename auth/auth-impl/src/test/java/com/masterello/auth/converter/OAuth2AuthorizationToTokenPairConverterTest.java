package com.masterello.auth.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.config.AuthorisationServerConfig;
import com.masterello.auth.domain.TokenPair;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthorizationToTokenPairConverterTest {

    private final Supplier<ObjectMapper> authServiceObjectMapper = new AuthorisationServerConfig().authServiceObjectMapper();

    private OAuth2AuthorizationToTokenPairConverter converter;

    @BeforeEach
    void setUp() {
        converter = new OAuth2AuthorizationToTokenPairConverter(authServiceObjectMapper);
    }

    @SneakyThrows
    @Test
    void toEntity() {
        MasterelloTestUser user = getUser();
        user.setEmailVerified(false);
        user.setStatus(UserStatus.BANNED);
        user.setRoles(Set.of(Role.USER, Role.WORKER, Role.ADMIN));

        TokenPair entity = converter.toEntity(getOAuthAuthorization(user));
        TokenPair expectedEntity = getTokenPair();
        assertThat(entity).usingRecursiveComparison().ignoringFields("id","accessTokenMetadata", "issuedAt")
                .isEqualTo(expectedEntity);

        assertEquals(
                authServiceObjectMapper.get().readValue(expectedEntity.getAccessTokenMetadata(), new TypeReference<Map<String, Object>>() {
                }),
                authServiceObjectMapper.get().readValue(entity.getAccessTokenMetadata(), new TypeReference<Map<String, Object>>() {
                })
        );
    }
}