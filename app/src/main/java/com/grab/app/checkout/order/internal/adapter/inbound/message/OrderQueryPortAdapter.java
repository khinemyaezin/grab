package com.grab.app.checkout.order.internal.adapter.inbound.message;

import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.GetOrderByIdPortMapper;
import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.GetOrderByOrderNumberPortMapper;
import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.ListCustomerOrdersPortMapper;
import com.grab.app.checkout.order.internal.adapter.inbound.message.mapper.SearchOrdersPortMapper;
import com.grab.app.checkout.order.port.message.OrderPlacementPort.OrderResult;
import com.grab.app.checkout.order.port.message.OrderQueryPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class OrderQueryPortAdapter implements OrderQueryPort {

    private final QueryBus queryBus;
    private final GetOrderByIdPortMapper getOrderByIdMapper;
    private final GetOrderByOrderNumberPortMapper getOrderByOrderNumberMapper;
    private final ListCustomerOrdersPortMapper listCustomerOrdersMapper;
    private final SearchOrdersPortMapper searchOrdersMapper;

    public OrderQueryPortAdapter(
            @Qualifier("checkoutQueryBus") QueryBus queryBus,
            GetOrderByIdPortMapper getOrderByIdMapper,
            GetOrderByOrderNumberPortMapper getOrderByOrderNumberMapper,
            ListCustomerOrdersPortMapper listCustomerOrdersMapper,
            SearchOrdersPortMapper searchOrdersMapper) {
        this.queryBus = queryBus;
        this.getOrderByIdMapper = getOrderByIdMapper;
        this.getOrderByOrderNumberMapper = getOrderByOrderNumberMapper;
        this.listCustomerOrdersMapper = listCustomerOrdersMapper;
        this.searchOrdersMapper = searchOrdersMapper;
    }

    @Override
    public Optional<OrderResult> findOrderById(Id orderId) {
        return Optional.empty();
    }

    @Override
    public Optional<OrderResult> findOrderByOrderNumber(String orderNumber) {
        return Optional.empty();
    }

    @Override
    public List<OrderResult> listCustomerOrders(Id customerId, int page, int size) {
        return Collections.emptyList();
    }

    @Override
    public List<OrderResult> searchOrders(OrderSearchCriteria criteria) {
        return Collections.emptyList();
    }
}
