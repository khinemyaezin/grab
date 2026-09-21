package com.grab.store.cart.internal.query.handler;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.port.inbound.GetCurrentCartUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.cart.internal.config.CartReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCurrentCartQueryHandler implements QueryHandler<GetCurrentCartQuery, CartResult> {
    private final GetCurrentCartUseCase getCurrentCartUseCase;

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
