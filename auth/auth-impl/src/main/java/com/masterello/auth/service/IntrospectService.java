package com.masterello.auth.service;

import com.masterello.auth.config.TokenProperties;
import com.masterello.auth.converter.AuthEntityToOAuth2AuthorizationConverter;
import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class IntrospectService implements AuthService {
    private final AuthorizationRepository authorizationRepository;
    private final AuthEntityToOAuth2AuthorizationConverter converter;
    private final TokenProperties tokenProperties;

    @Override
    public Optional<AuthData> validateToken(String token) {
        Optional<Authorization> optAuth = authorizationRepository.findByAccessTokenValue(token);
        if(optAuth.isEmpty()) {
            return Optional.empty();
        }

        Authorization auth = optAuth.get();
        OffsetDateTime accessTokenExpiresAt = Instant.now().plus(tokenProperties.getAccessTokenTtl())
                .atOffset(auth.getAccessTokenExpiresAt().getOffset());
        auth.setAccessTokenExpiresAt(accessTokenExpiresAt);

        auth.setRefreshTokenExpiresAt(Instant.now().plus(tokenProperties.getRefreshTokenTtl())
                .atOffset(auth.getRefreshTokenExpiresAt().getOffset()));

        val saved = authorizationRepository.save(auth);
        val oAuth2Authorization = converter.toOAuth2Authorization(saved);
        OAuth2ClientAuthenticationToken principal = (OAuth2ClientAuthenticationToken)oAuth2Authorization.getAttributes().get(Principal.class.getName());
        MasterelloUser user = (MasterelloUser) principal.getDetails();

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
