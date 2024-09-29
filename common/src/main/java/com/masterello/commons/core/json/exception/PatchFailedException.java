package com.masterello.commons.core.json.exception;

public class PatchFailedException extends RuntimeException {

    public PatchFailedException(String message) {
        super(message);
    }

    public PatchFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
