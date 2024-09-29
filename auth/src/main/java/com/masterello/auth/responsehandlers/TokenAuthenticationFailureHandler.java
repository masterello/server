package com.masterello.auth.responsehandlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.*;

@Component
public class TokenAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final HttpMessageConverter<OAuth2Error> errorHttpResponseConverter =
            new OAuth2ErrorHttpMessageConverter();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
        HttpStatus status = convertToHttpStatus(error.getErrorCode());
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        httpResponse.setStatusCode(status);
        this.errorHttpResponseConverter.write(error, null, httpResponse);
    }

    private static HttpStatus convertToHttpStatus(String errorCode) {
        return switch (errorCode) {
            case ACCESS_DENIED , UNAUTHORIZED_CLIENT, INSUFFICIENT_SCOPE, INVALID_TOKEN, INVALID_CLIENT, INVALID_GRANT ->
                    HttpStatus.UNAUTHORIZED;
            case SERVER_ERROR ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
            case TEMPORARILY_UNAVAILABLE ->
                HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
