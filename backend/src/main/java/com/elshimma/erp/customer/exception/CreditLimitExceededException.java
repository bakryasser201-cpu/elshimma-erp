package com.elshimma.erp.customer.exception;

public class CreditLimitExceededException extends RuntimeException {

    public CreditLimitExceededException(String message) {
        super(message);
    }
}
