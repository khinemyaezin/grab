package com.cart.application.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CartServiceError extends MessageSource permits
        CartServiceError.CartNotFound,
        CartServiceError.GuestTokenRequired,
        CartServiceError.SalesChannelRequired {

    record CartNotFound(String guestToken, String salesChannelId) implements CartServiceError {
        public ErrorCategory kind() {
            return ErrorCategory.NOT_FOUND;
        }

        public String code() {
            return "cart.service.not_found";
        }

        public Map<String, Object> args() {
            return Map.of("guestToken", guestToken, "salesChannelId", salesChannelId);
        }
    }

    record GuestTokenRequired() implements CartServiceError {
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        public String code() {
            return "cart.service.guest_token.required";
        }

        public Map<String, Object> args() {
            return Map.of();
        }
    }

    record SalesChannelRequired() implements CartServiceError {
        public ErrorCategory kind() {
            return ErrorCategory.BAD_REQUEST;
        }

        public String code() {
            return "cart.service.sales_channel.required";
        }

        public Map<String, Object> args() {
            return Map.of();
        }
    }
}
