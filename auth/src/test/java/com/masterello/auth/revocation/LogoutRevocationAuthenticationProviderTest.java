package com.masterello.auth.revocation;

import com.masterello.auth.utils.AuthTestDataProvider;
import com.masterello.user.value.MasterelloUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutRevocationAuthenticationProviderTest {

    @Mock
    private OAuth2AuthorizationService authorizationService;

    @InjectMocks
    private LogoutRevocationAuthenticationProvider authenticationProvider;

    @Test
    void authenticate_WithValidTokenAndClient_ReturnsRevocationAuthenticationToken() {
        // Arrange
        RegisteredClient client = getClient();
        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);
        when(clientToken.getRegisteredClient()).thenReturn(client);
        when(clientToken.isAuthenticated()).thenReturn(true);

        OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthenticationToken =
                new OAuth2TokenRevocationAuthenticationToken(AuthTestDataProvider.ACCESS_TOKEN,
                        clientToken, OAuth2TokenType.ACCESS_TOKEN.getValue());

        MasterelloUser user = getUser();
        OAuth2Authorization authorization = getOAuthAuthorization(user);

        when(authorizationService.findByToken(ACCESS_TOKEN, OAuth2TokenType.ACCESS_TOKEN)).thenReturn(authorization);

        // Act
        Authentication result = authenticationProvider.authenticate(tokenRevocationAuthenticationToken);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo(ACCESS_TOKEN);
        verify(authorizationService, times(1)).remove(eq(authorization));
    }

    @Test
    void authenticate_WithUnknownToken_ReturnsOriginalAuthenticationToken() {
        // Arrange
        RegisteredClient client = getClient();
        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);
        when(clientToken.getRegisteredClient()).thenReturn(client);
        when(clientToken.isAuthenticated()).thenReturn(true);

        OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthenticationToken =
                new OAuth2TokenRevocationAuthenticationToken(AuthTestDataProvider.ACCESS_TOKEN,
                        clientToken, OAuth2TokenType.ACCESS_TOKEN.getValue());

        when(authorizationService.findByToken(ACCESS_TOKEN, OAuth2TokenType.ACCESS_TOKEN)).thenReturn(null);

        // Act
        Authentication result = authenticationProvider.authenticate(tokenRevocationAuthenticationToken);

        // Assert
        assertThat(result).isEqualTo(tokenRevocationAuthenticationToken);
        verify(authorizationService, never()).remove(any());
    }

    @Test
    void authenticate_WithInvalidClient_ThrowsOAuth2AuthenticationException() {
        // Arrange
        OAuth2ClientAuthenticationToken clientToken = mock(OAuth2ClientAuthenticationToken.class);

        OAuth2TokenRevocationAuthenticationToken tokenRevocationAuthenticationToken =
                new OAuth2TokenRevocationAuthenticationToken(AuthTestDataProvider.ACCESS_TOKEN,
                        clientToken, OAuth2TokenType.ACCESS_TOKEN.getValue());

        // Act & Assert
        OAuth2Error error = assertThrows(OAuth2AuthenticationException.class,
                () -> authenticationProvider.authenticate(tokenRevocationAuthenticationToken)).getError();
        assertEquals(OAuth2ErrorCodes.INVALID_CLIENT, error.getErrorCode());
        verify(authorizationService, never()).remove(any());
    }

    @Test
    void supports_WithOAuth2TokenRevocationAuthenticationToken_ReturnsTrue() {
        // Arrange
        Class<?> authenticationClass = OAuth2TokenRevocationAuthenticationToken.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void supports_WithNonOAuth2TokenRevocationAuthenticationToken_ReturnsFalse() {
        // Arrange
        Class<?> authenticationClass = Authentication.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertThat(result).isFalse();
    }
}
