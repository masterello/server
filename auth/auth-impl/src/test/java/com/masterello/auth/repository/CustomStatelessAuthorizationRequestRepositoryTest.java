package com.masterello.auth.repository;

import com.masterello.auth.service.EncryptionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomStatelessAuthorizationRequestRepositoryTest {

    @Mock
    private EncryptionService encryptionService;

    private static final String jsonState = "{\"@class\":\"org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest\",\"authorizationUri\":\"https://example.com/auth\",\"authorizationGrantType\":{\"value\":\"authorization_code\"},\"responseType\":{\"value\":\"code\"},\"clientId\":\"client-id\",\"redirectUri\":null,\"scopes\":[\"java.util.Collections$UnmodifiableSet\",[]],\"state\":\"state\",\"additionalParameters\":{\"@class\":\"java.util.Collections$UnmodifiableMap\"},\"authorizationRequestUri\":\"https://example.com/auth?response_type=code&client_id=client-id&state=state\",\"attributes\":{\"@class\":\"java.util.Collections$UnmodifiableMap\"}}";

    @InjectMocks
    private CustomStatelessAuthorizationRequestRepository authorizationRequestRepository;

    @Test
    void loadAuthorizationRequest_WithValidCookie_ReturnsAuthorizationRequest() {
        // ArrangeTha
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("OAUTH", "encryptedValue")});

        OAuth2AuthorizationRequest decryptedAuthorizationRequest = buildAuthorizationRequest();
        when(encryptionService.decrypt("encryptedValue")).thenReturn(jsonState);

        // Act
        OAuth2AuthorizationRequest result = authorizationRequestRepository.loadAuthorizationRequest(request);

        // Assert
        assertThat(result).usingRecursiveComparison().isEqualTo(decryptedAuthorizationRequest);
    }

    @Test
    void loadAuthorizationRequest_WithInvalidCookie_ReturnsNull() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("INVALID_COOKIE", "encryptedValue")});

        // Act
        OAuth2AuthorizationRequest result = authorizationRequestRepository.loadAuthorizationRequest(request);

        // Assert
        assertNull(result);
    }

    @Test
    void saveAuthorizationRequest_WithAuthorizationRequest_SavesCookie() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(encryptionService.encrypt(any())).thenReturn("encryptedValue");

        OAuth2AuthorizationRequest authorizationRequest = buildAuthorizationRequest();

        // Act
        authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);

        // Assert
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    void saveAuthorizationRequest_WithNullAuthorizationRequest_RemovesCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Create an ArgumentCaptor for the Cookie class
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        // Act
        authorizationRequestRepository.saveAuthorizationRequest(null, request, response);

        // Assert
        // Capture the Cookie passed to response.addCookie()
        verify(response).addCookie(cookieCaptor.capture());

        // Retrieve the captured Cookie
        Cookie capturedCookie = cookieCaptor.getValue();

        // Verify the content of the Cookie
        assertEquals("OAUTH", capturedCookie.getName());
        assertEquals("-", capturedCookie.getValue());
        assertEquals(Duration.ZERO.toSeconds(), capturedCookie.getMaxAge());
    }

    @Test
    void removeAuthorizationRequest_RetrievesCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("OAUTH", "encryptedValue")});

        OAuth2AuthorizationRequest decryptedAuthorizationRequest = buildAuthorizationRequest();
        when(encryptionService.decrypt("encryptedValue")).thenReturn(jsonState);

        // Act
        OAuth2AuthorizationRequest result = authorizationRequestRepository.removeAuthorizationRequest(request, response);

        // Assert
        assertThat(result).usingRecursiveComparison().isEqualTo(decryptedAuthorizationRequest);

    }

    private OAuth2AuthorizationRequest buildAuthorizationRequest() {
        return OAuth2AuthorizationRequest
                .authorizationCode()
                .authorizationUri("https://example.com/auth")
                .clientId("client-id")
                .state("state")
                .build();
    }
}
