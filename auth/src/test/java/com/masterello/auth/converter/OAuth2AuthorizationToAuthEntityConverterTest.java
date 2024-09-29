package com.masterello.auth.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.config.AuthorisationServerConfig;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.mapper.PrincipalMapper;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthorizationToAuthEntityConverterTest {

    private final Supplier<ObjectMapper> authServiceObjectMapper = new AuthorisationServerConfig().authServiceObjectMapper();

    @Mock
    private PrincipalMapper principalMapper;

    private OAuth2AuthorizationToAuthEntityConverter converter;

    @BeforeEach
    void setUp() {
        converter = new OAuth2AuthorizationToAuthEntityConverter(authServiceObjectMapper, principalMapper);
    }

    @SneakyThrows
    @Test
    void toEntity() {
        MasterelloTestUser user = getUser();
        user.setEmailVerified(false);
        user.setStatus(UserStatus.BANNED);
        user.setRoles(Set.of(Role.USER, Role.WORKER, Role.ADMIN));
        RegisteredClient client = getClient();
        when(principalMapper.mapToSerializablePrincipal(getPrincipalToken(user, client)))
                .thenReturn(getSerializablePrincipal(user.getUuid().toString(), client.getId()));

        Authorization entity = converter.toEntity(getOAuthAuthorization(user));
        Authorization expectedEntity = getAuthorization();
        assertThat(entity).usingRecursiveComparison().ignoringFields("accessTokenMetadata")
                .isEqualTo(expectedEntity);

        assertEquals(
                authServiceObjectMapper.get().readValue(expectedEntity.getAccessTokenMetadata(), new TypeReference<Map<String, Object>>() {
                }),
                authServiceObjectMapper.get().readValue(entity.getAccessTokenMetadata(), new TypeReference<Map<String, Object>>() {
                })
        );
    }
}