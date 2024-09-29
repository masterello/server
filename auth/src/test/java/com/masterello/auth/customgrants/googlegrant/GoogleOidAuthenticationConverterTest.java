package com.masterello.auth.customgrants.googlegrant;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GoogleOidAuthenticationConverterTest {

    private final GoogleOidAuthenticationConverter converter = new GoogleOidAuthenticationConverter();

    @Test
    void convert_WithValidGoogleOidToken_ReturnsGoogleOidAuthenticationToken() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(GoogleOidAuthenticationConverter.GOOGLE_OID_GRANT_TYPE);
        when(request.getParameter(OAuth2ParameterNames.TOKEN)).thenReturn("valid_oid_token");

        Authentication clientPrincipal = new UsernamePasswordAuthenticationToken("client", "client_secret");
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        // Act
        Authentication result = converter.convert(request);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof GoogleOidAuthenticationToken);
        assertEquals(clientPrincipal, result.getPrincipal());

        GoogleOidAuthenticationToken googleOidToken = (GoogleOidAuthenticationToken) result;
        assertEquals("valid_oid_token", googleOidToken.getToken());
    }

    @Test
    void convert_WithInvalidGrantType_ReturnsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn("invalid_grant_type");

        // Act
        Authentication result = converter.convert(request);

        // Assert
        assertNull(result);
    }

    @Test
    void convert_WithNullToken_ReturnsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(GoogleOidAuthenticationConverter.GOOGLE_OID_GRANT_TYPE);
        when(request.getParameter(OAuth2ParameterNames.TOKEN)).thenReturn(null);

        // Act
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));
    }
}
