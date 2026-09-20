package com.grab.store.cart.internal.query.handler;

import com.cart.domain.enums.CartStatus;
import com.cart.domain.repository.CartRepository;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.cart.internal.command.CartResult;
import com.grab.store.cart.internal.config.CartTransactional;
import com.grab.store.cart.internal.exception.CartServiceError;
import com.grab.store.cart.internal.exception.CartServiceException;
import com.grab.store.cart.internal.query.GetCurrentCartQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCurrentCartQueryHandler implements QueryHandler<GetCurrentCartQuery, CartResult> {
    private final CartRepository carts;
    private final IdGenerator ids;

    @Override
    @CartTransactional(readOnly = true)
    public CartResult handle(GetCurrentCartQuery query) {
        if (query.guestToken() == null || query.guestToken().isBlank()) {
            throw new CartServiceException(new CartServiceError.GuestTokenRequired(), "Guest token is required");
        }
        if (query.salesChannelId() == null || query.salesChannelId().isBlank()) {
            throw new CartServiceException(new CartServiceError.SalesChannelRequired(), "Sales channel is required");
        }
        String regionId = query.regionId() == null || query.regionId().isBlank() ? "default" : query.regionId();
        return carts.findOpenByGuestToken(
                query.guestToken(),
                ids.convertIdFrom(query.salesChannelId()),
                ids.convertIdFrom(regionId),
                CartStatus.OPEN
        ).map(cart -> CartResult.from(cart, false)).orElseThrow(() -> new CartServiceException(
                new CartServiceError.CartNotFound(query.guestToken(), query.salesChannelId()),
                "Open cart not found"
        ));
    }

    @Override
    public Class<GetCurrentCartQuery> getQueryType() {
        return GetCurrentCartQuery.class;
    }
}
