package com.masterello.auth.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.config.AuthorisationServerConfig;
import com.masterello.auth.domain.TokenPair;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Optional;
import java.util.function.Supplier;

import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityToOAuth2AuthorizationConverterTest {

    private final Supplier<ObjectMapper> authServiceObjectMapper = new AuthorisationServerConfig().authServiceObjectMapper();
    @Mock
    private RegisteredClientRepository registeredClientRepository;
    @Mock
    private MasterelloUserService userService;

    private EntityToOAuth2AuthorizationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EntityToOAuth2AuthorizationConverter(authServiceObjectMapper, registeredClientRepository, userService);
    }

    @Test
    void toOAuth2Authorization() {
        RegisteredClient client = getClient();
        when(registeredClientRepository.findById(CLIENT_ID.toString())).thenReturn(client);
        MasterelloUser user = getUser();
        when(userService.findById(USER_ID)).thenReturn(Optional.of(user));
        TokenPair tokenPair = getTokenPair();

        OAuth2Authorization oAuth2Authorization = converter.toOAuth2Authorization(tokenPair.getAuthorization(), tokenPair);

        OAuth2Authorization expectedOAuth2Authorization = getOAuthAuthorization(user);
        assertEquals(expectedOAuth2Authorization, oAuth2Authorization);
    }

    @Test
    void toOAuth2Authorization_revoked() {
        RegisteredClient client = getClient();
        when(registeredClientRepository.findById(CLIENT_ID.toString())).thenReturn(client);
        MasterelloUser user = getUser();
        when(userService.findById(USER_ID)).thenReturn(Optional.of(user));
        TokenPair tokenPair = getTokenPair();
        tokenPair.setRevoked(true);

        OAuth2Authorization oAuth2Authorization = converter.toOAuth2Authorization(tokenPair.getAuthorization(), tokenPair);

        OAuth2Authorization expectedOAuth2Authorization = getRevokedOAuthAuthorization(user);
        assertEquals(expectedOAuth2Authorization, oAuth2Authorization);
    }
}