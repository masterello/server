package com.masterello.auth.service;

import com.masterello.auth.converter.AuthEntityToOAuth2AuthorizationConverter;
import com.masterello.auth.converter.OAuth2AuthorizationToAuthEntityConverter;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.repository.AuthorizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaOAuth2AuthorizationServiceTest {

    @Mock
    private OAuth2AuthorizationToAuthEntityConverter authorizationToEntityConverter;

    @Mock
    private AuthEntityToOAuth2AuthorizationConverter entityToAuthorisationConverter;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @InjectMocks
    private JpaOAuth2AuthorizationService authorizationService;

    @Test
    void save_ShouldSaveAuthorizationEntity() {
        // Arrange
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        Authorization authEntity = mock(Authorization.class);
        when(authorizationToEntityConverter.toEntity(authorization)).thenReturn(authEntity);

        // Act
        authorizationService.save(authorization);

        // Assert
        verify(authorizationRepository).save(authEntity);
    }

    @Test
    void remove_ShouldRemoveAuthorizationEntity() {
        // Arrange
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        String id = UUID.randomUUID().toString();
        when(authorization.getId()).thenReturn(id);
        // Act
        authorizationService.remove(authorization);

        // Assert
        verify(authorizationRepository).deleteById(id);
    }

    @Test
    void findById_WithValidId_ShouldReturnOAuth2Authorization() {
        // Arrange
        String validId = "validId";
        Authorization authorizationEntity = mock(Authorization.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(authorizationRepository.findById(validId)).thenReturn(Optional.ofNullable(authorizationEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(authorizationEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findById(validId);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnNull() {
        // Arrange
        String invalidId = "invalidId";
        when(authorizationRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act
        OAuth2Authorization result = authorizationService.findById(invalidId);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByToken_WithAccessToken_ShouldReturnOAuth2Authorization() {
        // Arrange
        String accessToken = "accessToken";
        Authorization authorizationEntity = mock(Authorization.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(authorizationRepository.findByAccessTokenValue(accessToken)).thenReturn(Optional.ofNullable(authorizationEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(authorizationEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(accessToken, OAuth2TokenType.ACCESS_TOKEN);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findByToken_WithRefreshToken_ShouldReturnOAuth2Authorization() {
        // Arrange
        String refreshToken = "refreshToken";
        Authorization authorizationEntity = mock(Authorization.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(authorizationRepository.findByRefreshTokenValue(refreshToken)).thenReturn(Optional.ofNullable(authorizationEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(authorizationEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findByToken_WithUnknownTokenType_ShouldReturnOAuth2Authorization() {
        // Arrange
        String token = "token";
        Authorization authorizationEntity = mock(Authorization.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(authorizationRepository.findByAccessTokenValueOrRefreshTokenValue(token, token)).thenReturn(Optional.ofNullable(authorizationEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(authorizationEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(token, null);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }
}
