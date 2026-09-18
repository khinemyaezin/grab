package com.grab.store.saleschannel.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SalesChannelServiceError extends MessageSource permits
        SalesChannelServiceError.ChannelNotFound,
        SalesChannelServiceError.MerchantScopeRequired {

    record ChannelNotFound(String salesChannelId) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "sc.service.channel.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("salesChannelId", salesChannelId);
        }
    }

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
}
