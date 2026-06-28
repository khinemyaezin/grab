package com.merchant.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface MerchantDomainError extends MessageSource permits
        MerchantDomainError.InvalidField,
        MerchantDomainError.InvalidStatusTransition,
        MerchantDomainError.IncompleteProfile,
        MerchantDomainError.DuplicateOpenApplication,
        MerchantDomainError.DuplicateRegistration,
        MerchantDomainError.ApplicantAccessForbidden {

    record InvalidField(String field) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        public String code() { return "mer.domain.field_invalid"; }
        public Map<String, Object> args() { return Map.of("field", field); }
    }

    record InvalidStatusTransition(String currentStatus, String requestedStatus) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.status_transition_invalid"; }
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record IncompleteProfile(String field) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.profile_incomplete"; }
        public Map<String, Object> args() { return Map.of("field", field); }
    }

    record DuplicateOpenApplication(String applicantUserId, String merchantType) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.domain.application.already_open"; }
        public Map<String, Object> args() {
            return Map.of("applicantUserId", applicantUserId, "merchantType", merchantType);
        }
    }

    record DuplicateRegistration(String countryCode, String registrationNumber) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.domain.registration.duplicate"; }
        public Map<String, Object> args() {
            return Map.of("countryCode", countryCode, "registrationNumber", registrationNumber);
        }
    }

    record ApplicantAccessForbidden(String merchantId) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.FORBIDDEN; }
        public String code() { return "mer.domain.applicant_access_forbidden"; }
        public Map<String, Object> args() { return Map.of("merchantId", merchantId); }
    }
}
