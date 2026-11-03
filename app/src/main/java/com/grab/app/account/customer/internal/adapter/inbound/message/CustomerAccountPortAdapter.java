package com.grab.app.account.customer.internal.adapter.inbound.message;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.grab.app.account.customer.internal.adapter.inbound.message.mapper.GetCustomerByIdPortMapper;
import com.grab.app.account.customer.internal.adapter.inbound.message.mapper.GetCustomerByUserIdPortMapper;
import com.grab.app.account.customer.port.message.CustomerAccountPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerAccountPortAdapter implements CustomerAccountPort {

    private final QueryBus queryBus;
    private final GetCustomerByIdPortMapper getCustomerByIdMapper;
    private final GetCustomerByUserIdPortMapper getCustomerByUserIdMapper;

    public CustomerAccountPortAdapter(
            @Qualifier("accountQueryBus") QueryBus queryBus,
            GetCustomerByIdPortMapper getCustomerByIdMapper,
            GetCustomerByUserIdPortMapper getCustomerByUserIdMapper) {
        this.queryBus = queryBus;
        this.getCustomerByIdMapper = getCustomerByIdMapper;
        this.getCustomerByUserIdMapper = getCustomerByUserIdMapper;
    }

    @Override
    public Optional<CustomerProfileResult> getCustomerById(Id customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        GetCustomerByIdQuery query = getCustomerByIdMapper.toQuery(customerId);
        CustomerView view = queryBus.dispatch(query);
        return Optional.ofNullable(getCustomerByIdMapper.toResponse(view));
    }

    @Override
    public Optional<CustomerProfileResult> getCustomerByUserId(Id userId) {
        if (userId == null) {
            return Optional.empty();
        }
        GetCustomerByUserIdQuery query = getCustomerByUserIdMapper.toQuery(userId);
        CustomerView view = queryBus.dispatch(query);
        return Optional.ofNullable(getCustomerByUserIdMapper.toResponse(view));
    }
}
