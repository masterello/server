package com.masterello.auth.service;

import com.masterello.auth.converter.EntityToOAuth2AuthorizationConverter;
import com.masterello.auth.converter.OAuth2AuthorizationToEntityConverter;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.TokenPair;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final OAuth2AuthorizationToEntityConverter oAuth2AuthorizationToEntityConverter;
    private final EntityToOAuth2AuthorizationConverter entityToOAuth2AuthorizationConverter;
    private final AuthorizationRepository authorizationRepository;
    private final TokenPairRepository tokenPairRepository;

    public static final OAuth2TokenType AUTHORIZATION_CODE_TOKEN_TYPE =
            new OAuth2TokenType(OAuth2ParameterNames.CODE);

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        val authzEntity = oAuth2AuthorizationToEntityConverter.toAuthorizationEntity(authorization);
        val savedAuthz = authorizationRepository.saveAndFlush(authzEntity);

        if(authorization.getAccessToken() != null || authorization.getRefreshToken() != null) {
            tokenPairRepository.revokeAllTokensByAuthorizationId(authzEntity.getId());
            val tokenPair = oAuth2AuthorizationToEntityConverter.toTokenPairEntity(authorization);
            tokenPair.setAuthorization(savedAuthz);
            tokenPairRepository.save(tokenPair);
        }

    }

    @Override
    @Transactional
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Nullable
    @Transactional
    @Override
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id)
                .map(this::toAuthorization)
                .orElse(null);
    }

    @Nullable
    @Transactional
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {

        if (AUTHORIZATION_CODE_TOKEN_TYPE.equals(tokenType)) {
            return authorizationRepository.findByAuthorizationCodeValue(token)
                    .map(this::toAuthorization)
                    .orElse(null);
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            return tokenPairRepository.findByAccessTokenValue(token)
                    .map(this::toAuthorization)
                    .orElse(null);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            return tokenPairRepository.findByRefreshTokenValue(token)
                    .map(this::toAuthorization)
                    .orElse(null);
        } else {
            return tokenPairRepository.findByAccessTokenValueOrRefreshTokenValue(token, token)
                    .map(this::toAuthorization)
                    .orElse(null);
        }
    }

    private OAuth2Authorization toAuthorization(Authorization authorization) {
        TokenPair tokenPair = tokenPairRepository.findByAuthorizationIdAndRevokedFalse(authorization.getId()).orElse(null);
        return entityToOAuth2AuthorizationConverter.toOAuth2Authorization(authorization, tokenPair);
    }

    private OAuth2Authorization toAuthorization(TokenPair tokenPair) {
        return entityToOAuth2AuthorizationConverter.toOAuth2Authorization(tokenPair.getAuthorization(), tokenPair);
    }
}
