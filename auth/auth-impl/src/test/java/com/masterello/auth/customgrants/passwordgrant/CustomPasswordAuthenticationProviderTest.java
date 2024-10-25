package com.masterello.auth.customgrants.passwordgrant;

import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.auth.utils.AuthTestDataProvider;
import com.masterello.user.service.AuthNService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.masterello.auth.utils.AuthTestDataProvider.getClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPasswordAuthenticationProviderTest {

    @Mock
    private OAuth2AuthorizationService authorizationService;
    @Mock
    private OAuth2TokenGenerator<OAuth2Token> tokenGenerator;
    @Mock
    private SecurityUserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomPasswordAuthenticationProvider authenticationProvider;

    @Test
    void authenticate_WithValidCredentials_ReturnsAuthentication() {
        // Arrange
        String username = "testUser";
        String password = "testPassword";

        OAuth2Token accessToken = AuthTestDataProvider.getAccessToken();
        OAuth2Token refreshToken = AuthTestDataProvider.getRefreshToken();
        when(tokenGenerator.generate(any()))
                .thenReturn(accessToken, refreshToken);


        OAuth2ClientAuthenticationToken clientToken = AuthTestDataProvider.prepareClientAuthData();

        CustomPasswordAuthenticationToken authenticationToken =
                new CustomPasswordAuthenticationToken(clientToken,
                        Map.of(OAuth2ParameterNames.USERNAME, username,
                                OAuth2ParameterNames.PASSWORD, password));

        when(userDetailsService.loadUserByUsername(username)).thenReturn(createTestUser(username, password));
        when(passwordEncoder.matches(password, password)).thenReturn(true);

        // Act
        Authentication result = authenticationProvider.authenticate(authenticationToken);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof OAuth2AccessTokenAuthenticationToken);
        assertEquals(accessToken, ((OAuth2AccessTokenAuthenticationToken) result).getAccessToken());
        assertEquals(refreshToken, ((OAuth2AccessTokenAuthenticationToken) result).getRefreshToken());
    }

    @Test
    void authenticate_WithUserNotFound_ThrowsUsernameNotFoundException() {
        // Arrange
        String username = "nonexistentUser";
        String password = "testPassword";

        RegisteredClient client = getClient();
        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);
        when(clientToken.getRegisteredClient()).thenReturn(client);
        when(clientToken.isAuthenticated()).thenReturn(true);

        CustomPasswordAuthenticationToken authenticationToken =
                new CustomPasswordAuthenticationToken(clientToken,
                        Map.of(OAuth2ParameterNames.USERNAME, username,
                                OAuth2ParameterNames.PASSWORD, password));

        when(userDetailsService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> authenticationProvider.authenticate(authenticationToken));
    }

    @Test
    void authenticate_WithWrongPassword() {
        // Arrange
        String username = "user";
        String wrongPassword = "wrongPassword";
        String password = "password";

        RegisteredClient client = getClient();
        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);
        when(clientToken.getRegisteredClient()).thenReturn(client);
        when(clientToken.isAuthenticated()).thenReturn(true);

        CustomPasswordAuthenticationToken authenticationToken =
                new CustomPasswordAuthenticationToken(clientToken,
                        Map.of(OAuth2ParameterNames.USERNAME, username,
                                OAuth2ParameterNames.PASSWORD, wrongPassword));

        when(userDetailsService.loadUserByUsername(username)).thenReturn(createTestUser(username, password));
        when(passwordEncoder.matches(wrongPassword, password)).thenReturn(false);

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> authenticationProvider.authenticate(authenticationToken));
    }

    @Test
    void supports_WithCustomPasswordAuthenticationToken_ReturnsTrue() {
        // Arrange
        Class<CustomPasswordAuthenticationToken> authenticationClass = CustomPasswordAuthenticationToken.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertTrue(result);
    }

    @Test
    void supports_WithDifferentAuthenticationToken_ReturnsFalse() {
        // Arrange
        Class<UsernamePasswordAuthenticationToken> authenticationClass = UsernamePasswordAuthenticationToken.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertFalse(result);
    }

    private SecurityUserDetails createTestUser(String username, String password) {
        return new SecurityUserDetails(MasterelloTestUser.builder()
                .uuid(UUID.randomUUID())
                .email(username)
                .password(password)
                .roles(Set.of(Role.USER))
                .build());
    }
}
