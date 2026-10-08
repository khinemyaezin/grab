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
        MerchantDomainError.StorefrontChannelAlreadyAttached,
        MerchantDomainError.CannotDemoteSoleAdmin,
        MerchantDomainError.DuplicateMerchantMember,
        MerchantDomainError.InvalidMemberStatusTransition,
        MerchantDomainError.InvitationExpired,
        MerchantDomainError.MemberNotFound {

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

    record CannotDemoteSoleAdmin(String merchantId, String memberId) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.member.cannot_demote_sole_admin"; }
        public Map<String, Object> args() {
            return Map.of("merchantId", merchantId, "memberId", memberId);
        }
    }

    record DuplicateMerchantMember(String merchantId, String userId) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "mer.domain.member.duplicate"; }
        public Map<String, Object> args() {
            return Map.of("merchantId", merchantId, "userId", userId);
        }
    }

    record InvalidMemberStatusTransition(String currentStatus, String requestedStatus) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.member.status_transition_invalid"; }
        public Map<String, Object> args() {
            return Map.of("currentStatus", currentStatus, "requestedStatus", requestedStatus);
        }
    }

    record InvitationExpired(String memberId, String expiresAt) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.BUSINESS_RULE; }
        public String code() { return "mer.domain.member.invitation_expired"; }
        public Map<String, Object> args() {
            return Map.of("memberId", memberId, "expiresAt", String.valueOf(expiresAt));
        }
    }

    record MemberNotFound(String memberId) implements MerchantDomainError {
        public ErrorCategory kind() { return ErrorCategory.NOT_FOUND; }
        public String code() { return "mer.domain.member.not_found"; }
        public Map<String, Object> args() {
            return Map.of("memberId", memberId);
        }
    }
}
