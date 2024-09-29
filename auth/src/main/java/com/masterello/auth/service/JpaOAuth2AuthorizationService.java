package com.masterello.auth.service;

import com.masterello.auth.converter.AuthEntityToOAuth2AuthorizationConverter;
import com.masterello.auth.converter.OAuth2AuthorizationToAuthEntityConverter;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.repository.AuthorizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final OAuth2AuthorizationToAuthEntityConverter authorizationToEntityConverter;
    private final AuthEntityToOAuth2AuthorizationConverter entityToAuthorisationConverter;
    private final AuthorizationRepository authorizationRepository;

    @Override
    public void save(OAuth2Authorization authorization) {
        Authorization entity = authorizationToEntityConverter.toEntity(authorization);
        authorizationRepository.save(entity);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        authorizationRepository.deleteById(authorization.getId());
    }

    @Nullable
    @Override
    public OAuth2Authorization findById(String id) {
        return authorizationRepository.findById(id)
                .map(this::toAuthorization)
                .orElse(null);
    }

    @Nullable
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            return authorizationRepository.findByAccessTokenValue(token)
                    .map(this::toAuthorization)
                    .orElse(null);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            return authorizationRepository.findByRefreshTokenValue(token)
                    .map(this::toAuthorization)
                    .orElse(null);
        } else {
            return authorizationRepository.findByAccessTokenValueOrRefreshTokenValue(token, token)
                    .map(this::toAuthorization)
                    .orElse(null);
        }
    }

    private OAuth2Authorization toAuthorization(Authorization authorization) {
        return entityToAuthorisationConverter.toOAuth2Authorization(authorization);
    }
}
