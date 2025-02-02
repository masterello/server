package com.masterello.auth.service;

import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthType;
import com.masterello.auth.data.AuthZRole;
import com.masterello.user.value.Role;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE;

@RequiredArgsConstructor
@Service
public class IntrospectService implements AuthService {
    private final JpaOAuth2AuthorizationService authorizationService;

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

        val principalToken = (MasterelloAuthenticationToken) auth.getAttributes().get(Principal.class.getName());
        val user = principalToken.getPrincipal();

        return Optional.ofNullable(AuthData.builder()
                .username(user.getUsername())
                .emailVerified(user.isEmailVerified())
                .userRoles(toAuthZRoles(user.getRoles()))
                .userId(user.getUuid())
                .authType(resolveAuthType(auth.getAuthorizationGrantType()))
                .build());

    }

    private AuthType resolveAuthType(AuthorizationGrantType authorizationGrantType) {
        if(authorizationGrantType.getValue().equals(PASSWORD_GRANT_TYPE)) {
            return AuthType.PASSWORD;
        } else {
            return AuthType.PASSWORDLESS;
        }
    }


    private List<AuthZRole> toAuthZRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .map(AuthZRole::valueOf)
                .toList();
    }
}
