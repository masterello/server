package com.masterello.auth.customgrants.googlegrant;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

@Getter
public class GoogleOidAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private static final long serialVersionUID = 3506080732586435489L;

    private final String token;

    public GoogleOidAuthenticationToken(Authentication clientPrincipal,
                                        Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType(GoogleOidAuthenticationConverter.GOOGLE_OID_GRANT_TYPE), clientPrincipal, additionalParameters);
        this.token = (String) additionalParameters.get(OAuth2ParameterNames.TOKEN);

    }

}
