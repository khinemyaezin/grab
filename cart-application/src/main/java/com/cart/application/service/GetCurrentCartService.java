package com.cart.application.service;

import com.cart.application.exception.CartServiceError;
import com.cart.application.exception.CartServiceException;
import com.cart.application.model.read.CartResult;
import com.cart.application.model.read.GetCurrentCartQuery;
import com.cart.application.port.inbound.GetCurrentCartUseCase;
import com.cart.application.port.outbound.CartQueryPort;
import com.cart.domain.enums.CartStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCurrentCartService implements GetCurrentCartUseCase {
    private final CartQueryPort cartQueryPort;

    @Override
    public CartResult execute(GetCurrentCartQuery query) {
        if (query.guestToken() == null || query.guestToken().isBlank()) {
            throw new CartServiceException(new CartServiceError.GuestTokenRequired(), "Guest token is required");
        }
        if (query.salesChannelId() == null || query.salesChannelId().isBlank()) {
            throw new CartServiceException(new CartServiceError.SalesChannelRequired(), "Sales channel is required");
        }
        String regionId = query.regionId() == null || query.regionId().isBlank() ? "default" : query.regionId();
        return cartQueryPort.findOpenByGuestToken(
                query.guestToken(),
                query.salesChannelId(),
                regionId,
                CartStatus.OPEN
        ).map(cart -> CartResult.from(cart, false)).orElseThrow(() -> new CartServiceException(
                new CartServiceError.CartNotFound(query.guestToken(), query.salesChannelId()),
                "Open cart not found"
        ));
    }
}
