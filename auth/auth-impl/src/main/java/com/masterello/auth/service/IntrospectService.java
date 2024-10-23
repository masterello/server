package com.masterello.auth.service;

import com.masterello.auth.config.TokenProperties;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.user.value.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class IntrospectService implements AuthService {
    private final JpaOAuth2AuthorizationService authorizationService;
    private final TokenProperties tokenProperties;

    @Override
    public Optional<AuthData> validateToken(String token) {
        OAuth2Authorization auth = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);

        if (auth == null) {
            return Optional.empty();
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                auth.getAccessToken();
        if (!accessToken.isActive()) {
            return Optional.empty();
        }

        OAuth2AccessToken aToken = accessToken.getToken();
        Instant newAccessTokenExpiresAt = Instant.now().plus(tokenProperties.getAccessTokenTtl());

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                auth.getRefreshToken();
        if (refreshToken != null && !refreshToken.isActive()) {
            return Optional.empty();
        }

        OAuth2RefreshToken rToken = refreshToken.getToken();
        Instant newRefreshTokenExpiresAt = Instant.now().plus(tokenProperties.getRefreshTokenTtl());


        val updatedAuth = OAuth2Authorization.from(auth)
                .accessToken(new OAuth2AccessToken(aToken.getTokenType(), aToken.getTokenValue(),
                        aToken.getIssuedAt(), newAccessTokenExpiresAt, aToken.getScopes()))
                .refreshToken(new OAuth2RefreshToken(rToken.getTokenValue(), rToken.getIssuedAt(),
                        newRefreshTokenExpiresAt))
                .build();

        authorizationService.save(updatedAuth);

        val principalToken = (MasterelloAuthenticationToken) updatedAuth.getAttributes().get(Principal.class.getName());
        val user = principalToken.getPrincipal();

        return Optional.ofNullable(AuthData.builder()
                .username(user.getUsername())
                .emailVerified(user.isEmailVerified())
                .userRoles(toAuthZRoles(user.getRoles()))
                .userId(user.getUuid())
                .build());

    }


    private List<AuthZRole> toAuthZRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .map(AuthZRole::valueOf)
                .toList();
    }
}
