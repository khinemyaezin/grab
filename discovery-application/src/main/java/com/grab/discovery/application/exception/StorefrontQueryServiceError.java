package com.grab.discovery.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface StorefrontQueryServiceError extends MessageSource permits
        StorefrontQueryServiceError.SalesChannelRequired,
        StorefrontQueryServiceError.ChannelDisabled,
        StorefrontQueryServiceError.OfferNotFound,
        StorefrontQueryServiceError.InvalidPublishableKey,
        StorefrontQueryServiceError.ContextChannelDisabled,
        StorefrontQueryServiceError.CountryCodeInvalid,
        StorefrontQueryServiceError.LocaleInvalid,
        StorefrontQueryServiceError.RegionUnavailable,
        StorefrontQueryServiceError.DefaultRegionUnavailable,
        StorefrontQueryServiceError.UnsupportedChannelSelector {

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

    record InvalidPublishableKey() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.UNAUTHORIZED;
        }

        @Override
        public String code() {
            return "sfq.service.publishable_key.invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record ContextChannelDisabled() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sfq.service.context.channel_disabled";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record CountryCodeInvalid() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sfq.service.context.country_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record LocaleInvalid() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sfq.service.context.locale_invalid";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record RegionUnavailable() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "sfq.service.context.region_unavailable";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record DefaultRegionUnavailable() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "sfq.service.context.default_region_unavailable";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record UnsupportedChannelSelector() implements StorefrontQueryServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sfq.service.context.channel_selector_unsupported";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
