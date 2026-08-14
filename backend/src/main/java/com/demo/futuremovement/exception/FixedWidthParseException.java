package com.demo.futuremovement.exception;

public class FixedWidthParseException extends RuntimeException {

    public FixedWidthParseException(String message) {
        super(message);
    }

    public FixedWidthParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
