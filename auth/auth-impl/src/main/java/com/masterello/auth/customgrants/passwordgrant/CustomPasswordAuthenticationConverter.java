package com.masterello.auth.customgrants.passwordgrant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomPasswordAuthenticationConverter implements AuthenticationConverter {

    public static final String PASSWORD_GRANT_TYPE = "password";

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {

        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);

        if (!PASSWORD_GRANT_TYPE.equals(grantType)) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(request.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // login (REQUIRED)
        String username = Optional.ofNullable(jsonNode.get(OAuth2ParameterNames.USERNAME))
                .map(JsonNode::asText)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST));

        // password (REQUIRED)
        String password = Optional.ofNullable(jsonNode.get(OAuth2ParameterNames.PASSWORD))
                .map(JsonNode::asText)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST));

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OAuth2ParameterNames.USERNAME, username);
        additionalParameters.put(OAuth2ParameterNames.PASSWORD, password);
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        return new CustomPasswordAuthenticationToken(clientPrincipal, additionalParameters);
    }
}
