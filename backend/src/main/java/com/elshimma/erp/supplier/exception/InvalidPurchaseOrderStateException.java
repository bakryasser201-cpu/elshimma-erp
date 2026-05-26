package com.elshimma.erp.supplier.exception;

public class InvalidPurchaseOrderStateException extends RuntimeException {
    public InvalidPurchaseOrderStateException(String message) {
        super(message);
    }
}
