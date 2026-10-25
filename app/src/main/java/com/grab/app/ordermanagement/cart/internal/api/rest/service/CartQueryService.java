package com.grab.app.ordermanagement.cart.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.ordermanagement.cart.internal.api.rest.dto.response.CartResponse;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.grab.app.ordermanagement.cart.internal.api.rest.mapper.CartResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartQueryService {
    private final QueryBus queryBus;

    public CartResponse current(String guestToken, String salesChannelId, String regionId) {
        return CartResponseMapper.toResponse(queryBus.dispatch(
                new GetCurrentCartQuery(guestToken, salesChannelId, regionId)
        ));
    }
}
