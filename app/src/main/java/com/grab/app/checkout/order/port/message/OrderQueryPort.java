package com.grab.app.checkout.order.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Order Queries and Customer History.
 */
public interface OrderQueryPort {

    Optional<OrderPlacementPort.OrderResult> findOrderById(Id orderId);

    Optional<OrderPlacementPort.OrderResult> findOrderByOrderNumber(String orderNumber);

    List<OrderPlacementPort.OrderResult> listCustomerOrders(Id customerId, int page, int size);

    List<OrderPlacementPort.OrderResult> searchOrders(OrderSearchCriteria criteria);

    record OrderSearchCriteria(
            Id merchantId,
            String status,
            int page,
            int size
    ) {}
}

