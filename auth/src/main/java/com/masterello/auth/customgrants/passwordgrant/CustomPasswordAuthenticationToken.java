package com.masterello.auth.customgrants.passwordgrant;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

import static com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE;

@Getter
public class CustomPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private static final long serialVersionUID = -8151681029723948692L;
    private final String username;
    private final String password;

    public CustomPasswordAuthenticationToken(Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType(PASSWORD_GRANT_TYPE), clientPrincipal, additionalParameters);
        this.username = (String) additionalParameters.get(OAuth2ParameterNames.USERNAME);
        this.password = (String) additionalParameters.get(OAuth2ParameterNames.PASSWORD);
    }

}
