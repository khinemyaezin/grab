package com.saleschannel.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;
import com.grab.framework.id.Id;

import java.util.Map;

public sealed interface SalesChannelServiceError extends MessageSource permits
        SalesChannelServiceError.ChannelNotFound,
        SalesChannelServiceError.RegionNotActive,
        SalesChannelServiceError.MultipleDefaultRegions,
        SalesChannelServiceError.PrimaryMarketRegionUnavailable,
        SalesChannelServiceError.PublishableKeyNotFound,
        SalesChannelServiceError.PublishableKeyDigestConflict {

    record PublishableKeyNotFound(Id keyId) implements SalesChannelServiceError {
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        public String code() {
            return "sc.service.publishable_key.not_found";
        }

        public Map<String, Object> args() {
            String value = keyId.getValue();
            return Map.of("keyId", value);
        }
    }

    record PublishableKeyDigestConflict() implements SalesChannelServiceError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "sc.service.publishable_key.digest_conflict";
        }

        public Map<String, Object> args() {
            return Map.of();
        }
    }

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

    record RegionNotActive(String regionId) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "sc.service.region.not_active";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("regionId", regionId);
        }
    }

    record MultipleDefaultRegions(String salesChannelId) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        @Override
        public String code() {
            return "sc.service.region.multiple_defaults";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("salesChannelId", salesChannelId);
        }
    }

    record PrimaryMarketRegionUnavailable(String countryCode) implements SalesChannelServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        @Override
        public String code() {
            return "sc.service.primary_market.region_unavailable";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("countryCode", countryCode == null ? "" : countryCode);
        }
    }
}
