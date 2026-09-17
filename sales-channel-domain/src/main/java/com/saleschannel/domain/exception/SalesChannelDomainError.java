package com.saleschannel.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SalesChannelDomainError extends MessageSource permits
        SalesChannelDomainError.MerchantRequired,
        SalesChannelDomainError.MarketplaceMustBePlatformOwned,
        SalesChannelDomainError.InvalidStatusTransition,
        SalesChannelDomainError.DuplicateWebsite,
        SalesChannelDomainError.DuplicateMarketplace,
        SalesChannelDomainError.ChannelDisabled,
        SalesChannelDomainError.ChannelOwnershipForbidden {

    record MerchantRequired(String type) implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        public String code() {
            return "sc.domain.merchant_required";
        }

        public Map<String, Object> args() {
            return Map.of("type", type);
        }
    }

    record MarketplaceMustBePlatformOwned() implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        public String code() {
            return "sc.domain.marketplace_platform_owned";
        }

        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record InvalidStatusTransition(String currentStatus, String requestedStatus) implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        public String code() {
            return "sc.domain.status_transition_invalid";
        }

        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record DuplicateWebsite(String merchantId) implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "sc.domain.website.duplicate";
        }

        public Map<String, Object> args() {
            return Map.of("merchantId", merchantId);
        }
    }

    record DuplicateMarketplace() implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.CONFLICT;
        }

        public String code() {
            return "sc.domain.marketplace.duplicate";
        }

        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record ChannelDisabled(String salesChannelId) implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.BUSINESS_RULE;
        }

        public String code() {
            return "sc.domain.channel.disabled";
        }

        public Map<String, Object> args() {
            return Map.of("salesChannelId", salesChannelId);
        }
    }

    record ChannelOwnershipForbidden(String salesChannelId, String merchantId) implements SalesChannelDomainError {
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        public String code() {
            return "sc.domain.channel.ownership_forbidden";
        }

        public Map<String, Object> args() {
            return Map.of("salesChannelId", salesChannelId, "merchantId", merchantId);
        }
    }
}
