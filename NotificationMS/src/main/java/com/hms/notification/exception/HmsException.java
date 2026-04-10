package com.hms.notification.exception;

public class HmsException extends Exception {
    private static final long serialVersionUID = 1L;

    public HmsException(String message) {
        super(message);
    }

    public HmsException(String message, Throwable cause) {
        super(message, cause);
    }
}

