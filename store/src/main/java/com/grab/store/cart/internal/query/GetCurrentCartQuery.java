package com.grab.store.cart.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.cart.internal.command.CartResult;

public record GetCurrentCartQuery(
        String guestToken,
        String salesChannelId,
        String regionId
) implements Query<CartResult> {
}
