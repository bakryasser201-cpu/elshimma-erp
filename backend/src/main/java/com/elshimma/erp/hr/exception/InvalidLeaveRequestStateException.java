package com.elshimma.erp.hr.exception;

public class InvalidLeaveRequestStateException extends RuntimeException {
    public InvalidLeaveRequestStateException(String message) {
        super(message);
    }
}
