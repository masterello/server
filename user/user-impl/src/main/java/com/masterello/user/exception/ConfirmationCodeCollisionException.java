package com.masterello.user.exception;

public class ConfirmationCodeCollisionException extends RuntimeException {

    public ConfirmationCodeCollisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
