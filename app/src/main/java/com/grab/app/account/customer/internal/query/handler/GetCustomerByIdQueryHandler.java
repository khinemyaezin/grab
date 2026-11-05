package com.grab.app.account.customer.internal.query.handler;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByIdQuery;
import com.customer.application.port.inbound.GetCustomerByIdUseCase;
import com.grab.app.account.customer.internal.config.CustomerReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCustomerByIdQueryHandler implements QueryHandler<GetCustomerByIdQuery, CustomerView> {

    private final GetCustomerByIdUseCase getCustomerByIdUseCase;

    public GetCustomerByIdQueryHandler(GetCustomerByIdUseCase getCustomerByIdUseCase) {
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
    }

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

