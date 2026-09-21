package com.grab.store.cart.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.cart.internal.api.rest.dto.response.CartResponse;
import com.cart.application.model.read.GetCurrentCartQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartQueryService {
    private final QueryBus queryBus;

    public CartResponse current(String guestToken, String salesChannelId, String regionId) {
        return CartCommandService.toResponse(queryBus.dispatch(
                new GetCurrentCartQuery(guestToken, salesChannelId, regionId)
        ));
    }
}
