package com.elshimma.erp.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrentInventoryModificationException extends RuntimeException {

    public ConcurrentInventoryModificationException(Long inventoryId) {
        super("Inventory " + inventoryId + " is being modified by another transaction. Please retry the operation.");
    }
}
