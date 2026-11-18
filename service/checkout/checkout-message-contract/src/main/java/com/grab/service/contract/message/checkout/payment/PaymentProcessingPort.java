package com.grab.service.contract.message.checkout.payment;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Universal Java Port Interface for Payment Authorization and Capture.
 */
public interface PaymentProcessingPort extends Remote {

    PaymentResult authorizePayment(AuthorizePaymentMessage message) throws RemoteException;

    PaymentResult capturePayment(Id paymentId, BigDecimal amount) throws RemoteException;

    void refundPayment(Id paymentId, RefundPaymentMessage message) throws RemoteException;

    PaymentResult findPaymentById(Id paymentId) throws RemoteException;

    record AuthorizePaymentMessage(
            Id orderId,
            BigDecimal amount,
            String currencyCode,
            String paymentMethodToken
    ) implements Serializable {}

    record RefundPaymentMessage(
            BigDecimal amount,
            String reason
    ) implements Serializable {}

    record PaymentResult(
            Id id,
            Id orderId,
            BigDecimal amount,
            String currencyCode,
            String status, // AUTHORIZED, CAPTURED, REFUNDED, FAILED
            String transactionReference,
            Instant createdAt
    ) implements Serializable {}
}

