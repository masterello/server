package com.masterello.auth.customgrants.googlegrant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GoogleOidAuthenticationConverter implements AuthenticationConverter {

    public static final String GOOGLE_OID_GRANT_TYPE = "google_oid";

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {

        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);

        if (!GOOGLE_OID_GRANT_TYPE.equals(grantType)) {
            return null;
        }

        String oid_token = request.getParameter(OAuth2ParameterNames.TOKEN);
        if (!StringUtils.hasText(oid_token)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OAuth2ParameterNames.TOKEN, oid_token);


        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        return new GoogleOidAuthenticationToken(clientPrincipal, additionalParameters);
    }
}
