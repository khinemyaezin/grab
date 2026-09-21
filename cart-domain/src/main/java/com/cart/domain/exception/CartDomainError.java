package com.cart.domain.exception;

import com.grab.framework.exception.ErrorCategory;
import com.grab.framework.exception.MessageSource;

import java.util.Map;

public sealed interface CartDomainError extends MessageSource permits
        CartDomainError.ChannelDisabled,
        CartDomainError.NotPublished,
        CartDomainError.NotPriced,
        CartDomainError.InsufficientStock,
        CartDomainError.MixedSeller,
        CartDomainError.InvalidQuantity,
        CartDomainError.CartNotOpen {

    record ChannelDisabled(String salesChannelId) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.channel.disabled"; }
        public Map<String, Object> args() { return Map.of("salesChannelId", salesChannelId); }
    }

    record NotPublished(String variantId, String salesChannelId) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.not_published"; }
        public Map<String, Object> args() {
            return Map.of("variantId", variantId, "salesChannelId", salesChannelId);
        }
    }

    record NotPriced(String variantId) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.not_priced"; }
        public Map<String, Object> args() { return Map.of("variantId", variantId); }
    }

    record InsufficientStock(String sku, int requested, int available) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.insufficient_stock"; }
        public Map<String, Object> args() {
            return Map.of("sku", sku, "requested", requested, "available", available);
        }
    }

    record MixedSeller(String existingSellerId, String incomingSellerId) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.mixed_seller"; }
        public Map<String, Object> args() {
            return Map.of("existingSellerId", existingSellerId, "incomingSellerId", incomingSellerId);
        }
    }

    record InvalidQuantity(int quantity) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.BAD_REQUEST; }
        public String code() { return "cart.domain.invalid_quantity"; }
        public Map<String, Object> args() { return Map.of("quantity", quantity); }
    }

    record CartNotOpen(String cartId, String status) implements CartDomainError {
        public ErrorCategory kind() { return ErrorCategory.CONFLICT; }
        public String code() { return "cart.domain.not_open"; }
        public Map<String, Object> args() { return Map.of("cartId", cartId, "status", status); }
    }
}
