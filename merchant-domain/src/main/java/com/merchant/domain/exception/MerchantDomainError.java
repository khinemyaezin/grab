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
        MerchantDomainError.ApplicantAccessForbidden,
        MerchantDomainError.DuplicateSlug,
        MerchantDomainError.MerchantNotOperational,
        MerchantDomainError.InvalidStorefrontTransition,
        MerchantDomainError.StorefrontChannelAlreadyAttached {

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

    record DuplicateSlug(String slug) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.domain.storefront.slug_duplicate"; }
        public Map<String, Object> args() { return Map.of("slug", slug); }
    }

    record MerchantNotOperational(String merchantId, String status) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.merchant_not_operational"; }
        public Map<String, Object> args() { return Map.of("merchantId", merchantId, "status", status); }
    }

    record InvalidStorefrontTransition(String currentStatus, String requestedStatus) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.storefront.status_transition_invalid"; }
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record StorefrontChannelAlreadyAttached(String storefrontId) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.domain.storefront.channel_already_attached"; }
        public Map<String, Object> args() {
            return Map.of("storefrontId", storefrontId);
        }
    }
}
