package com.grab.app.checkout.payment.internal.adapter.inbound.message;

import com.grab.app.checkout.payment.internal.adapter.inbound.message.mapper.AuthorizePaymentPortMapper;
import com.grab.app.checkout.payment.internal.adapter.inbound.message.mapper.CapturePaymentPortMapper;
import com.grab.app.checkout.payment.internal.adapter.inbound.message.mapper.GetPaymentByIdPortMapper;
import com.grab.app.checkout.payment.internal.adapter.inbound.message.mapper.RefundPaymentPortMapper;
import com.grab.app.checkout.payment.port.message.PaymentProcessingPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component
public class PaymentProcessingPortAdapter implements PaymentProcessingPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final IdGenerator idGenerator;
    private final AuthorizePaymentPortMapper authorizePaymentMapper;
    private final CapturePaymentPortMapper capturePaymentMapper;
    private final RefundPaymentPortMapper refundPaymentMapper;
    private final GetPaymentByIdPortMapper getPaymentByIdMapper;

    public PaymentProcessingPortAdapter(
            @Qualifier("checkoutCommandBus") CommandBus commandBus,
            @Qualifier("checkoutQueryBus") QueryBus queryBus,
            IdGenerator idGenerator,
            AuthorizePaymentPortMapper authorizePaymentMapper,
            CapturePaymentPortMapper capturePaymentMapper,
            RefundPaymentPortMapper refundPaymentMapper,
            GetPaymentByIdPortMapper getPaymentByIdMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.idGenerator = idGenerator;
        this.authorizePaymentMapper = authorizePaymentMapper;
        this.capturePaymentMapper = capturePaymentMapper;
        this.refundPaymentMapper = refundPaymentMapper;
        this.getPaymentByIdMapper = getPaymentByIdMapper;
    }

    @Override
    public PaymentResult authorizePayment(AuthorizePaymentMessage message) {
        Id paymentId = idGenerator.generateId();
        return new PaymentResult(
                paymentId,
                message.orderId(),
                message.amount(),
                message.currencyCode(),
                "AUTHORIZED",
                "TXN-" + System.currentTimeMillis(),
                Instant.now()
        );
    }

    @Override
    public PaymentResult capturePayment(Id paymentId, BigDecimal amount) {
        return new PaymentResult(
                paymentId,
                null,
                amount,
                "USD",
                "CAPTURED",
                "TXN-" + System.currentTimeMillis(),
                Instant.now()
        );
    }

    @Override
    public void refundPayment(Id paymentId, RefundPaymentMessage message) {
        // Payment refund
    }

    @Override
    public Optional<PaymentResult> findPaymentById(Id paymentId) {
        return Optional.empty();
    }
}
