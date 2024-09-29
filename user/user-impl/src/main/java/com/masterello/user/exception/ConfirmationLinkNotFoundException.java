package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ConfirmationLinkNotFoundException extends RuntimeException {

    public ConfirmationLinkNotFoundException(String message) {
        super(message);
    }
}
