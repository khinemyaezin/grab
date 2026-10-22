package com.saleschannel.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface SalesChannelServiceError extends MessageSource permits
        SalesChannelServiceError.ChannelNotFound,
        SalesChannelServiceError.RegionNotActive,
        SalesChannelServiceError.MultipleDefaultRegions {

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
}
