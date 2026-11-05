package com.grab.app.checkout.cart.internal.query.handler;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.port.inbound.GetCurrentCartUseCase;
import com.grab.app.checkout.cart.internal.config.CartReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetCurrentCartQueryHandler implements QueryHandler<GetCurrentCartQuery, CartResult> {

    private final GetCurrentCartUseCase getCurrentCartUseCase;

    public GetCurrentCartQueryHandler(GetCurrentCartUseCase getCurrentCartUseCase) {
        this.getCurrentCartUseCase = getCurrentCartUseCase;
    }

    @Override
    @CartReadTransactional
    public CartResult handle(GetCurrentCartQuery query) {
        return getCurrentCartUseCase.execute(query);
    }

    @Override
    public Class<GetCurrentCartQuery> getQueryType() {
        return GetCurrentCartQuery.class;
    }
}
