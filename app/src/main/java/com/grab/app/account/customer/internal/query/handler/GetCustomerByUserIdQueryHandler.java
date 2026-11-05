package com.grab.app.account.customer.internal.query.handler;

import com.customer.application.model.read.CustomerView;
import com.customer.application.model.read.GetCustomerByUserIdQuery;
import com.customer.application.port.inbound.GetCustomerByUserIdUseCase;
import com.grab.app.account.customer.internal.config.CustomerReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCustomerByUserIdQueryHandler implements QueryHandler<GetCustomerByUserIdQuery, CustomerView> {

    private final GetCustomerByUserIdUseCase getCustomerByUserIdUseCase;

    public GetCustomerByUserIdQueryHandler(GetCustomerByUserIdUseCase getCustomerByUserIdUseCase) {
        this.getCustomerByUserIdUseCase = getCustomerByUserIdUseCase;
    }

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

