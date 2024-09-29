package com.masterello.auth.revocation;

import com.masterello.auth.exception.TokenCookieNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutRevocationEndpointAuthenticationConverterTest {

    @InjectMocks
    private LogoutRevocationEndpointAuthenticationConverter authenticationConverter;

    @Mock
    private HttpServletRequest request;

    @Test
    void convert_WithValidTokenInCookie_ReturnsOAuth2TokenRevocationAuthenticationToken() {
        // Arrange
        Authentication clientPrincipal = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        String validToken = "validToken";
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(M_TOKEN_COOKIE, validToken)});

        // Act
        Authentication result = authenticationConverter.convert(request);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo(validToken);
        assertThat((result).getPrincipal()).isEqualTo(clientPrincipal);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getTokenTypeHint())
                .isEqualTo(OAuth2TokenType.ACCESS_TOKEN.getValue());
    }

    @Test
    void convert_WithMissingTokenCookie_ThrowsTokenCookieNotFoundException() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act & Assert
        assertThatThrownBy(() -> authenticationConverter.convert(request))
                .isInstanceOf(TokenCookieNotFoundException.class)
                .hasMessage("Token cookie not found");
    }
}
