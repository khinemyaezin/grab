package com.region.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface RegionServiceError extends MessageSource permits
        RegionServiceError.RegionNotFound,
        RegionServiceError.PersistenceFailure {

    record RegionNotFound(String regionId) implements RegionServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "rgn.service.region.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("regionId", regionId);
        }
    }

    record PersistenceFailure(String resource) implements RegionServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.INTERNAL;
        }

        @Override
        public String code() {
            return "rgn.persistence.failure";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("resource", resource);
        }
    }
}
