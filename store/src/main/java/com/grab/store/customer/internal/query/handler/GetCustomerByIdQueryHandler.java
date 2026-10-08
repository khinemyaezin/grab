package com.grab.store.customer.internal.query.handler;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.customer.application.port.inbound.GetCustomerByIdUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.customer.internal.config.CustomerReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCustomerByIdQueryHandler implements QueryHandler<GetCustomerByIdQuery, CustomerView> {

    private final GetCustomerByIdUseCase getCustomerByIdUseCase;

    @Override
    @CustomerReadTransactional
    public CustomerView handle(GetCustomerByIdQuery query) {
        return getCustomerByIdUseCase.execute(query);
    }

    @Override
    public Class<GetCustomerByIdQuery> getQueryType() {
        return GetCustomerByIdQuery.class;
    }
}
