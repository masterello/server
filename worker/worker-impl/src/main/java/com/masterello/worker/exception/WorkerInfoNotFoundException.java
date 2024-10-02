package com.masterello.worker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkerInfoNotFoundException extends RuntimeException {

    public WorkerInfoNotFoundException(String message) {
        super(message);
    }
}
