package com.merchant.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface MerchantServiceError extends MessageSource permits
        MerchantServiceError.MerchantNotFound,
        MerchantServiceError.MerchantScopeForbidden,
        MerchantServiceError.ApplicantAccessForbidden,
        MerchantServiceError.StorefrontNotFound {

    record MerchantNotFound(String merchantId) implements MerchantServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "mer.service.account.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("merchantId", merchantId);
        }
    }

    record MerchantScopeForbidden(String platformCode, String scopeKey, String scopeId)
            implements MerchantServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "mer.service.scope.forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of(
                    "platformCode", platformCode,
                    "scopeKey", scopeKey,
                    "scopeId", scopeId
            );
        }
    }

    record ApplicantAccessForbidden(String merchantId) implements MerchantServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.FORBIDDEN;
        }

        @Override
        public String code() {
            return "mer.service.applicant.forbidden";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("merchantId", merchantId);
        }
    }

    record StorefrontNotFound(String storefrontId) implements MerchantServiceError {
        @Override
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        @Override
        public String code() {
            return "mer.service.storefront.not_found";
        }

        @Override
        public Map<String, Object> args() {
            return Map.of("storefrontId", storefrontId);
        }
    }
}
