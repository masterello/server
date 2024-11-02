package com.masterello.auth.service;

import com.masterello.auth.converter.OAuth2AuthorizationToTokenPairConverter;
import com.masterello.auth.converter.TokenPairEntityToOAuth2AuthorizationConverter;
import com.masterello.auth.domain.TokenPair;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final OAuth2AuthorizationToTokenPairConverter authorizationToTokenPairConverter;
    private final TokenPairEntityToOAuth2AuthorizationConverter tokenPairEntityToOAuth2AuthorizationConverter;
    private final AuthorizationRepository authorizationRepository;
    private final TokenPairRepository tokenPairRepository;

    @Override
    @Transactional
    public void save(OAuth2Authorization authorization) {
        val tokenPair = authorizationToTokenPairConverter.toEntity(authorization);
        tokenPairRepository.revokeAllTokensByAuthorizationId(tokenPair.getAuthorization().getId());
        authorizationRepository.saveAndFlush(tokenPair.getAuthorization());
        tokenPairRepository.save(tokenPair);
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
        return tokenPairRepository.findByAuthorizationIdAndRevokedFalse(id)
                .map(this::toAuthorization)
                .orElse(null);
    }

    @Nullable
    @Transactional
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
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

    private OAuth2Authorization toAuthorization(TokenPair tokenPair) {
        return tokenPairEntityToOAuth2AuthorizationConverter.toOAuth2Authorization(tokenPair);
    }
}
