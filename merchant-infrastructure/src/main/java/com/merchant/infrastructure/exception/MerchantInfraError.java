package com.merchant.infrastructure.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface MerchantInfraError extends MessageSource permits
        MerchantInfraError.PersistenceConflict,
        MerchantInfraError.PersistenceInternal {

    record PersistenceConflict(String resource) implements MerchantInfraError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.infra.persistence.conflict"; }
        public Map<String, Object> args() { return Map.of("resource", resource); }
    }

    record PersistenceInternal(String resource) implements MerchantInfraError {
        public ErrorCategory kind() { return ErrorCategory.INTERNAL; }
        public String code() { return "mer.infra.persistence.internal"; }
        public Map<String, Object> args() { return Map.of("resource", resource); }
    }
}
