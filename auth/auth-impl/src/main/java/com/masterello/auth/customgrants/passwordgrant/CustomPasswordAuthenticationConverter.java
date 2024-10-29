package com.masterello.auth.customgrants.passwordgrant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

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

        Credentials result = getCredentials(request);

        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OAuth2ParameterNames.USERNAME, result.username());
        additionalParameters.put(OAuth2ParameterNames.PASSWORD, result.password());
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        return new CustomPasswordAuthenticationToken(clientPrincipal, additionalParameters);
    }

    private static Credentials getCredentials(HttpServletRequest request) {
        if(request.getHeader(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE))  {
            return getCredentialsFromForm(request);
        } else if (request.getHeader(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON_VALUE)) {
            return getCredentialsFromBody(request);
        } else {
            throw new UnsupportedMediaTypeStatusException("Token request supports only json and encoded form");
        }
    }

    private static Credentials getCredentialsFromForm(HttpServletRequest request) {
        return new Credentials(request.getParameter(OAuth2ParameterNames.USERNAME),
                request.getParameter(OAuth2ParameterNames.PASSWORD));
    }

    private static Credentials getCredentialsFromBody(HttpServletRequest request) {
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
        Credentials result = new Credentials(username, password);
        return result;
    }



    private record Credentials(String username, String password) {
    }
}
