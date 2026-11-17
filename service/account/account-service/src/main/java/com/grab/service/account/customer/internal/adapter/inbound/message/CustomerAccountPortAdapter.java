package com.grab.service.account.customer.internal.adapter.inbound.message;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.grab.service.account.customer.internal.adapter.inbound.message.mapper.GetCustomerByIdPortMapper;
import com.grab.service.account.customer.internal.adapter.inbound.message.mapper.GetCustomerByUserIdPortMapper;
import com.grab.service.contract.message.account.customer.CustomerAccountPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Optional;

@Component
@RmiExport
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
    public CustomerProfileResult getCustomerById(Id customerId) {
        if (customerId == null) {
            return null;
        }
        GetCustomerByIdQuery query = getCustomerByIdMapper.toQuery(customerId);
        CustomerView view = queryBus.dispatch(query);
        return getCustomerByIdMapper.toResponse(view);
    }

    @Override
    public CustomerProfileResult getCustomerByUserId(Id userId) {
        if (userId == null) {
            return null;
        }
        GetCustomerByUserIdQuery query = getCustomerByUserIdMapper.toQuery(userId);
        CustomerView view = queryBus.dispatch(query);
        return getCustomerByUserIdMapper.toResponse(view);
    }
}
