package com.masterello.commons.core.sort.exception;

import java.util.List;

public class SortValidationException extends RuntimeException {

    public SortValidationException(List<String> fields) {
        super(String.format("Fields %s are not allowed for sorting", fields));
    }
}
