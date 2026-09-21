package com.grab.store.storefrontquery.internal.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface StorefrontQueryServiceError extends MessageSource permits
        StorefrontQueryServiceError.SalesChannelRequired,
        StorefrontQueryServiceError.ChannelDisabled,
        StorefrontQueryServiceError.OfferNotFound {

    record SalesChannelRequired() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sfq.service.sales_channel.required";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record ChannelDisabled(String salesChannelId) implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "sfq.service.channel.disabled";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("salesChannelId", salesChannelId);
        }
    }

    record OfferNotFound(String slug, String salesChannelId) implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "sfq.service.offer.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("slug", slug, "salesChannelId", salesChannelId);
        }
    }
}
