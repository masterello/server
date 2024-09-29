package com.masterello.auth.customgrants.passwordgrant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomPasswordAuthenticationConverterTest {

    private final CustomPasswordAuthenticationConverter converter = new CustomPasswordAuthenticationConverter();

    @SneakyThrows
    @Test
    void convert_WithValidPasswordGrantType_ReturnsCustomPasswordAuthenticationToken() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE);

        String requestBody = "{\"username\":\"user\",\"password\":\"password\"}";
        InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(inputStream));

        Authentication clientPrincipal = new UsernamePasswordAuthenticationToken("client", "client_secret");
        SecurityContextHolder.getContext().setAuthentication(clientPrincipal);

        // Act
        Authentication result = converter.convert(request);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof CustomPasswordAuthenticationToken);
        assertEquals(clientPrincipal, result.getPrincipal());

        CustomPasswordAuthenticationToken customPasswordToken = (CustomPasswordAuthenticationToken) result;
        assertEquals("user", customPasswordToken.getUsername());
        assertEquals("password", customPasswordToken.getPassword());
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

    @SneakyThrows
    @Test
    void convert_WithMissingUsername_ThrowsOAuth2AuthenticationException() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE);

        String requestBody = "{\"password\":\"password\"}";
        InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(inputStream));

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));
    }

    @SneakyThrows
    @Test
    void convert_WithMissingPassword_ThrowsOAuth2AuthenticationException() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)).thenReturn(CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE);

        String requestBody = "{\"username\":\"user\"}";
        InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(inputStream));

        // Act & Assert
        assertThrows(OAuth2AuthenticationException.class, () -> converter.convert(request));
    }
}
