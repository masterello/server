package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class OAuthRegistrationException extends RuntimeException {

    public OAuthRegistrationException(String message) {
        super(message);
    }
}
