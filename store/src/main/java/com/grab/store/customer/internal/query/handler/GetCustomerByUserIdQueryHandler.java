package com.grab.store.customer.internal.query.handler;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.customer.application.port.inbound.GetCustomerByUserIdUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.customer.internal.config.CustomerReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCustomerByUserIdQueryHandler implements QueryHandler<GetCustomerByUserIdQuery, CustomerView> {

    private final GetCustomerByUserIdUseCase getCustomerByUserIdUseCase;

    @Override
    @CustomerReadTransactional
    public CustomerView handle(GetCustomerByUserIdQuery query) {
        return getCustomerByUserIdUseCase.execute(query);
    }

    @Override
    public Class<GetCustomerByUserIdQuery> getQueryType() {
        return GetCustomerByUserIdQuery.class;
    }
}
