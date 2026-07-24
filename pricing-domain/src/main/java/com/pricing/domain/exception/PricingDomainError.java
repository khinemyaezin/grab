package com.pricing.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface PricingDomainError extends MessageSource permits
        PricingDomainError.InvalidField,
        PricingDomainError.PriceNotFound,
        PricingDomainError.DuplicatePrice,
        PricingDomainError.InvalidQuantityRange,
        PricingDomainError.InvalidPriceListWindow {

    record InvalidField(String field) implements PricingDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "pri.domain.field_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("field", field);
        }
    }

    record PriceNotFound(String priceId) implements PricingDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "pri.domain.price.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("priceId", priceId);
        }
    }

    record DuplicatePrice(String priceId) implements PricingDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        @Override
        public String code() {
            return "pri.domain.price.duplicate";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("priceId", priceId);
        }
    }

    record InvalidQuantityRange(Integer minQuantity, Integer maxQuantity) implements PricingDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "pri.domain.quantity_range_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "minQuantity", minQuantity == null ? "" : minQuantity,
                    "maxQuantity", maxQuantity == null ? "" : maxQuantity
            );
        }
    }

    record InvalidPriceListWindow(String startsAt, String endsAt) implements PricingDomainError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "pri.domain.price_list.window_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "startsAt", startsAt == null ? "" : startsAt,
                    "endsAt", endsAt == null ? "" : endsAt
            );
        }
    }
}
