package com.elshimma.erp.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long inventoryId, String requested, String available) {
        super(String.format(
                "Insufficient stock for inventory %d: requested %s, available %s",
                inventoryId, requested, available));
    }
}
