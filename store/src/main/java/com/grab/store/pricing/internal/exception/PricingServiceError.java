package com.grab.store.pricing.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface PricingServiceError extends MessageSource permits
        PricingServiceError.PriceSetNotFound,
        PricingServiceError.PriceListNotFound,
        PricingServiceError.PricePreferenceNotFound,
        PricingServiceError.CurrencyRequired {

    record PriceSetNotFound(String priceSetId) implements PricingServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "pri.service.price_set.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("priceSetId", priceSetId);
        }
    }

    record PriceListNotFound(String priceListId) implements PricingServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "pri.service.price_list.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("priceListId", priceListId);
        }
    }

    record PricePreferenceNotFound(String pricePreferenceId) implements PricingServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "pri.service.price_preference.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("pricePreferenceId", pricePreferenceId);
        }
    }

    record CurrencyRequired() implements PricingServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "pri.service.calculate.currency_required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
