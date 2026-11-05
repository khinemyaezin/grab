package com.grab.app.checkout.order.internal.adapter.inbound.message;

import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.CancelOrderPortMapper;
import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.PlaceOrderPortMapper;
import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.UpdateOrderStatusPortMapper;
import com.grab.app.checkout.order.port.message.OrderPlacementPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

@Component
public class OrderPlacementPortAdapter implements OrderPlacementPort {

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final PlaceOrderPortMapper placeOrderMapper;
    private final CancelOrderPortMapper cancelOrderMapper;
    private final UpdateOrderStatusPortMapper updateStatusMapper;

    public OrderPlacementPortAdapter(
            @Qualifier("checkoutCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            PlaceOrderPortMapper placeOrderMapper,
            CancelOrderPortMapper cancelOrderMapper,
            UpdateOrderStatusPortMapper updateStatusMapper) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
        this.placeOrderMapper = placeOrderMapper;
        this.cancelOrderMapper = cancelOrderMapper;
        this.updateStatusMapper = updateStatusMapper;
    }

    @Override
    public OrderResult placeOrderFromCart(PlaceOrderMessage message) {
        Id orderId = idGenerator.generateId();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        return new OrderResult(
                orderId,
                orderNumber,
                message.customerId(),
                null,
                null,
                "USD",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "PENDING",
                Collections.emptyList(),
                Instant.now()
        );
    }

    @Override
    public void cancelOrder(Id orderId, String reason) {
        // Order cancellation
    }

    @Override
    public void updateOrderStatus(Id orderId, String newStatus) {
        // Order status update
    }
}
