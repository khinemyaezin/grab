package com.catalog.domain.exception;

import com.catalog.domain.aggregate.ProductStatus;
import com.grab.framework.exception.DomainException;
import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public class InvalidProductStatusTransitionException extends DomainException {
    public InvalidProductStatusTransitionException(ProductStatus status, ProductStatus newStatus) {
        super(
                new InvalidProductStatusTransitionError(status, newStatus),
                "Invalid status transition from " + status + " to " + newStatus
        );
    }
    record InvalidProductStatusTransitionError(ProductStatus status, ProductStatus newStatus) implements MessageSource {
        private static final String CODE = "exception.catalog.domain.invalid_product_status_transaction_error";
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of( "status", status.name(), "newStatus", newStatus.name());
        }
    }
}
