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

    private static final String M_TOKEN_COOKIE = "M_TOKEN";
    private static final String R_TOKEN_COOKIE = "R_TOKEN";

    @Test
    void convert_WithValidAccessTokenInCookie_ReturnsAccessTokenRevocationAuthenticationToken() {
        // Arrange
        Authentication clientPrincipal = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        String validAccessToken = "validAccessToken";
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(M_TOKEN_COOKIE, validAccessToken)});

        // Act
        Authentication result = authenticationConverter.convert(request);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo(validAccessToken);
        assertThat(result.getPrincipal()).isEqualTo(clientPrincipal);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getTokenTypeHint())
                .isEqualTo(OAuth2TokenType.ACCESS_TOKEN.getValue());
    }

    @Test
    void convert_WithValidRefreshTokenInCookie_ReturnsRefreshTokenRevocationAuthenticationToken() {
        // Arrange
        Authentication clientPrincipal = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        String validRefreshToken = "validRefreshToken";
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(R_TOKEN_COOKIE, validRefreshToken)});

        // Act
        Authentication result = authenticationConverter.convert(request);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo(validRefreshToken);
        assertThat(result.getPrincipal()).isEqualTo(clientPrincipal);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getTokenTypeHint())
                .isEqualTo(OAuth2TokenType.REFRESH_TOKEN.getValue());
    }

    @Test
    void convert_WithBothCookiesPresent_UsesAccessTokenOverRefreshToken() {
        // Arrange
        Authentication clientPrincipal = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie(M_TOKEN_COOKIE, accessToken),
                new Cookie(R_TOKEN_COOKIE, refreshToken)
        });

        // Act
        Authentication result = authenticationConverter.convert(request);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo(accessToken);
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

    @Test
    void convert_WithNullCookies_ThrowsTokenCookieNotFoundException() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> authenticationConverter.convert(request))
                .isInstanceOf(TokenCookieNotFoundException.class)
                .hasMessage("Token cookie not found");
    }

    @Test
    void convert_WithEmptyAccessToken_UsesRefreshTokenIfPresent() {
        // Arrange
        Authentication clientPrincipal = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        Cookie emptyAccessTokenCookie = new Cookie(M_TOKEN_COOKIE, "   "); // blank token
        Cookie validRefreshTokenCookie = new Cookie(R_TOKEN_COOKIE, "refreshToken123");

        when(request.getCookies()).thenReturn(new Cookie[]{emptyAccessTokenCookie, validRefreshTokenCookie});

        // Act
        Authentication result = authenticationConverter.convert(request);

        // Assert
        assertThat(result).isInstanceOf(OAuth2TokenRevocationAuthenticationToken.class);
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getToken()).isEqualTo("refreshToken123");
        assertThat(((OAuth2TokenRevocationAuthenticationToken) result).getTokenTypeHint())
                .isEqualTo(OAuth2TokenType.REFRESH_TOKEN.getValue());
    }
}
