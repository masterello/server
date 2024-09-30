package com.masterello.commons.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorisedException extends RuntimeException {

    public UnauthorisedException(String message) {
        super(message);
    }
}
