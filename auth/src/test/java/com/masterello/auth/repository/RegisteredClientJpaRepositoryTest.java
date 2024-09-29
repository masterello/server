package com.masterello.auth.repository;

import com.masterello.auth.domain.MasterelloRegisteredClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST;

@ExtendWith(MockitoExtension.class)
class RegisteredClientJpaRepositoryTest {

    @Mock
    private MasterelloRegisteredClientRepository clientRepository;

    @Mock
    private TokenSettings tokenSettings;

    @Mock
    private ClientSettings clientSettings;

    @InjectMocks
    private RegisteredClientJpaRepository registeredClientRepository;

    @Test
    void save_ValidRegisteredClient_CallsRepositorySave() {
        // Arrange
        RegisteredClient registeredClient = createSampleRegisteredClient();

        // Act
        registeredClientRepository.save(registeredClient);

        // Assert
        verify(clientRepository).save(any(MasterelloRegisteredClient.class));
    }

    @Test
    void save_NullRegisteredClient_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> registeredClientRepository.save(null));
    }

    @Test
    void findById_ValidId_ReturnsRegisteredClient() {
        // Arrange
        String clientId = "test-client";
        MasterelloRegisteredClient masterelloRegisteredClient = createSampleMasterelloRegisteredClient();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(masterelloRegisteredClient));

        // Act
        RegisteredClient result = registeredClientRepository.findById(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("Test Client", result.getClientName());
        assertEquals("secret", result.getClientSecret());
        assertThat(result.getAuthorizationGrantTypes()).containsExactlyInAnyOrder(AUTHORIZATION_CODE, REFRESH_TOKEN);
        assertThat(result.getClientAuthenticationMethods()).containsExactlyInAnyOrder(CLIENT_SECRET_BASIC, CLIENT_SECRET_POST);
    }

    @Test
    void findById_NullId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> registeredClientRepository.findById(null));
    }

    @Test
    void findByClientId_ValidClientId_ReturnsRegisteredClient() {
        // Arrange
        String clientId = "test-client";
        MasterelloRegisteredClient masterelloRegisteredClient = createSampleMasterelloRegisteredClient();
        when(clientRepository.findByClientId(clientId)).thenReturn(Optional.of(masterelloRegisteredClient));

        // Act
        RegisteredClient result = registeredClientRepository.findByClientId(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals("Test Client", result.getClientName());
        assertEquals("secret", result.getClientSecret());
        assertThat(result.getAuthorizationGrantTypes()).containsExactlyInAnyOrder(AUTHORIZATION_CODE, REFRESH_TOKEN);
        assertThat(result.getClientAuthenticationMethods()).containsExactlyInAnyOrder(CLIENT_SECRET_BASIC, CLIENT_SECRET_POST);
    }

    @Test
    void findByClientId_NullClientId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> registeredClientRepository.findByClientId(null));
    }

    // Helper method to create a sample RegisteredClient
    private RegisteredClient createSampleRegisteredClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("test-client")
                .clientName("Test Client")
                .clientSecret("secret")
                .clientAuthenticationMethod(CLIENT_SECRET_BASIC)
                .authorizationGrantType(AUTHORIZATION_CODE)
                .redirectUri("https://example.com/callback")
                .build();
    }

    // Helper method to create a sample MasterelloRegisteredClient
    private MasterelloRegisteredClient createSampleMasterelloRegisteredClient() {
        MasterelloRegisteredClient masterelloRegisteredClient = new MasterelloRegisteredClient();
        masterelloRegisteredClient.setId(UUID.randomUUID().toString());
        masterelloRegisteredClient.setClientId("test-client");
        masterelloRegisteredClient.setClientName("Test Client");
        masterelloRegisteredClient.setClientSecret("secret");
        masterelloRegisteredClient.setClientAuthenticationMethods("client_secret_basic,client_secret_post");
        masterelloRegisteredClient.setAuthorizationGrantTypes("authorization_code,refresh_token");
        masterelloRegisteredClient.setRedirectUris("https://example.com/callback");
        return masterelloRegisteredClient;
    }
}
