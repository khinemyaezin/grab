package com.grab.app.checkout.payment.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Universal Java Port Interface for Payment Authorization and Capture.
 */
public interface PaymentProcessingPort {

    PaymentResult authorizePayment(AuthorizePaymentMessage message);

    PaymentResult capturePayment(Id paymentId, BigDecimal amount);

    void refundPayment(Id paymentId, RefundPaymentMessage message);

    Optional<PaymentResult> findPaymentById(Id paymentId);

    record AuthorizePaymentMessage(
            Id orderId,
            BigDecimal amount,
            String currencyCode,
            String paymentMethodToken
    ) {}

    record RefundPaymentMessage(
            BigDecimal amount,
            String reason
    ) {}

    record PaymentResult(
            Id id,
            Id orderId,
            BigDecimal amount,
            String currencyCode,
            String status, // AUTHORIZED, CAPTURED, REFUNDED, FAILED
            String transactionReference,
            Instant createdAt
    ) {}
}

