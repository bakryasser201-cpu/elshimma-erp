package com.elshimma.erp.customer.validation;

import org.springframework.stereotype.Component;

@Component
public class CustomerValidation {

    public String normalizeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
