package com.grab.app.channel.saleschannel.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SalesChannelServiceError extends MessageSource permits
        SalesChannelServiceError.MerchantScopeRequired,
        SalesChannelServiceError.UnsupportedPublishableKeySort {

    record MerchantScopeRequired(String scopeKey, String scopeId) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "sc.service.merchant_scope.required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("scopeKey", scopeKey, "scopeId", scopeId);
        }
    }

    record UnsupportedPublishableKeySort(String property) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sc.service.publishable_key.unsupported_sort";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("property", property);
        }
    }
}
