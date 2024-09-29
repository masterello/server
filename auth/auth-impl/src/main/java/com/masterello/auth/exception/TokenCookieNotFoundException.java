package com.masterello.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TokenCookieNotFoundException extends RuntimeException {

    public TokenCookieNotFoundException(String message) {
        super(message);
    }
}
