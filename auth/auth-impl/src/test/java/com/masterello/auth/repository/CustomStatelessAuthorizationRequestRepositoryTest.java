package com.masterello.auth.repository;

import com.masterello.auth.domain.AuthorizationRequestEntity;
import com.masterello.auth.service.EncryptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomStatelessAuthorizationRequestRepositoryTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private AuthorizationRequestEntityRepository entityRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CustomStatelessAuthorizationRequestRepository repository;

    private static final String STATE = "state";
    private static final String ENCRYPTED_JSON = "encryptedJson";
    private static final String PLAIN_JSON = "{\"@class\":\"org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest\",\"authorizationUri\":\"https://example.com/auth\",\"authorizationGrantType\":{\"value\":\"authorization_code\"},\"responseType\":{\"value\":\"code\"},\"clientId\":\"client-id\",\"redirectUri\":null,\"scopes\":[\"java.util.Collections$UnmodifiableSet\",[]],\"state\":\"state\",\"additionalParameters\":{\"@class\":\"java.util.Collections$UnmodifiableMap\"},\"authorizationRequestUri\":\"https://example.com/auth?response_type=code&client_id=client-id&state=state\",\"attributes\":{\"@class\":\"java.util.Collections$UnmodifiableMap\"}}";

    private OAuth2AuthorizationRequest authRequest;

    @BeforeEach
    void setup() {
        authRequest = OAuth2AuthorizationRequest
                .authorizationCode()
                .authorizationUri("https://example.com/auth")
                .clientId("client-id")
                .state(STATE)
                .build();
    }

    @Test
    void loadAuthorizationRequest_withValidState_returnsRequest() throws Exception {
        AuthorizationRequestEntity entity = new AuthorizationRequestEntity(STATE, ENCRYPTED_JSON, Instant.now());
        when(request.getParameter("state")).thenReturn(STATE);
        when(entityRepository.findById(STATE)).thenReturn(Optional.of(entity));
        when(encryptionService.decrypt(ENCRYPTED_JSON)).thenReturn(PLAIN_JSON);

        OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

        assertThat(result).usingRecursiveComparison().isEqualTo(authRequest);
        verify(request).setAttribute(eq(CustomStatelessAuthorizationRequestRepository.CACHED_AUTH_REQUEST_ATTRIBUTE), any());
    }

    @Test
    void loadAuthorizationRequest_withNoState_returnsNull() {
        when(request.getParameter("state")).thenReturn(null);

        OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

        assertNull(result);
        verifyNoInteractions(entityRepository);
    }

    @Test
    void saveAuthorizationRequest_savesToDatabase() throws Exception {
        when(encryptionService.encrypt(any())).thenReturn(ENCRYPTED_JSON);

        repository.saveAuthorizationRequest(authRequest, request, response);

        verify(entityRepository).save(argThat(entity ->
                entity.getState().equals(STATE) &&
                        entity.getRequestJson().equals(ENCRYPTED_JSON) &&
                        entity.getExpiresAt().isAfter(Instant.now())
        ));
    }

    @Test
    void saveAuthorizationRequest_withNullRequest_doesNothing() {
        repository.saveAuthorizationRequest(null, request, response);

        verifyNoInteractions(entityRepository);
    }

    @Test
    void removeAuthorizationRequest_removesFromDatabase() throws Exception {
        AuthorizationRequestEntity entity = new AuthorizationRequestEntity(STATE, ENCRYPTED_JSON, Instant.now());
        when(request.getParameter("state")).thenReturn(STATE);
        when(entityRepository.findById(STATE)).thenReturn(Optional.of(entity));
        when(encryptionService.decrypt(ENCRYPTED_JSON)).thenReturn(PLAIN_JSON);

        OAuth2AuthorizationRequest result = repository.removeAuthorizationRequest(request, response);

        assertThat(result).usingRecursiveComparison().isEqualTo(authRequest);
        verify(entityRepository).deleteById(STATE);
    }

    @Test
    void removeAuthorizationRequest_withNullState_returnsNull() {
        when(request.getParameter("state")).thenReturn(null);

        OAuth2AuthorizationRequest result = repository.removeAuthorizationRequest(request, response);

        assertNull(result);
        verifyNoInteractions(entityRepository);
    }
}
