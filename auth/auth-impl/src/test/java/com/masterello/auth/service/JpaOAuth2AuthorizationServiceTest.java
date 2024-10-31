package com.masterello.auth.service;

import com.masterello.auth.converter.OAuth2AuthorizationToTokenPairConverter;
import com.masterello.auth.converter.TokenPairEntityToOAuth2AuthorizationConverter;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.TokenPair;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
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
    private OAuth2AuthorizationToTokenPairConverter authorizationToEntityConverter;

    @Mock
    private TokenPairEntityToOAuth2AuthorizationConverter entityToAuthorisationConverter;

    @Mock
    private AuthorizationRepository authorizationRepository;

    @Mock
    private TokenPairRepository tokenPairRepository;

    @InjectMocks
    private JpaOAuth2AuthorizationService authorizationService;

    @Test
    void save_ShouldSaveAuthorizationEntity() {
        // Arrange
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);

        Authorization authorizationEntity = mock(Authorization.class);
        String authId = UUID.randomUUID().toString();
        when(authorizationEntity.getId()).thenReturn(authId);
        TokenPair tokenPairEntity = mock(TokenPair.class);
        when(tokenPairEntity.getAuthorization()).thenReturn(authorizationEntity);
        when(authorizationToEntityConverter.toEntity(authorization)).thenReturn(tokenPairEntity);

        // Act
        authorizationService.save(authorization);

        // Assert
        verify(tokenPairRepository).revokeAllTokensByAuthorizationId(authId);
        verify(authorizationRepository).saveAndFlush(authorizationEntity);
        verify(tokenPairRepository).save(tokenPairEntity);
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
        verify(tokenPairRepository).deleteAllTokensByAuthorizationId(id);
        verify(authorizationRepository).deleteById(id);
    }

    @Test
    void findById_WithValidId_ShouldReturnOAuth2Authorization() {
        // Arrange
        String validId = "validId";
        TokenPair tokenPairEntity = mock(TokenPair.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(tokenPairRepository.findByAuthorizationIdAndRevokedFalse(validId)).thenReturn(Optional.of(tokenPairEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(tokenPairEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findById(validId);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findById_WithInvalidId_ShouldReturnNull() {
        // Arrange
        String invalidId = "invalidId";
        when(tokenPairRepository.findByAuthorizationIdAndRevokedFalse(invalidId)).thenReturn(Optional.empty());

        // Act
        OAuth2Authorization result = authorizationService.findById(invalidId);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void findByToken_WithAccessToken_ShouldReturnOAuth2Authorization() {
        // Arrange
        String accessToken = "accessToken";
        TokenPair tokenPairEntity = mock(TokenPair.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(tokenPairRepository.findByAccessTokenValue(accessToken)).thenReturn(Optional.of(tokenPairEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(tokenPairEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(accessToken, OAuth2TokenType.ACCESS_TOKEN);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findByToken_WithRefreshToken_ShouldReturnOAuth2Authorization() {
        // Arrange
        String refreshToken = "refreshToken";
        TokenPair tokenPairEntity = mock(TokenPair.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(tokenPairRepository.findByRefreshTokenValue(refreshToken)).thenReturn(Optional.of(tokenPairEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(tokenPairEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }

    @Test
    void findByToken_WithUnknownTokenType_ShouldReturnOAuth2Authorization() {
        // Arrange
        String token = "token";
        TokenPair tokenPairEntity = mock(TokenPair.class);
        OAuth2Authorization expectedAuthorization = mock(OAuth2Authorization.class);
        when(tokenPairRepository.findByAccessTokenValueOrRefreshTokenValue(token, token)).thenReturn(Optional.of(tokenPairEntity));
        when(entityToAuthorisationConverter.toOAuth2Authorization(tokenPairEntity)).thenReturn(expectedAuthorization);

        // Act
        OAuth2Authorization result = authorizationService.findByToken(token, null);

        // Assert
        assertThat(result).isEqualTo(expectedAuthorization);
    }
}
