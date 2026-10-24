package com.grab.store.cart.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.cart.internal.api.rest.dto.request.AddItemToCartRequest;
import com.grab.store.cart.internal.api.rest.dto.response.CartResponse;
import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.grab.store.cart.internal.api.rest.mapper.CartResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartCommandService {
    private final CommandBus commandBus;

    public CartResponse addItem(String guestToken, AddItemToCartRequest request) {
        return CartResponseMapper.toResponse(commandBus.dispatch(new AddItemToCartCommand(
                guestToken,
                request.salesChannelId(),
                request.regionId(),
                request.currencyCode(),
                request.variantId(),
                request.quantity(),
                request.displayedAmount()
        )));
    }
}
