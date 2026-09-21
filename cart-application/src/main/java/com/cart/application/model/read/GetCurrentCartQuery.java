package com.cart.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetCurrentCartQuery(
        String guestToken,
        String salesChannelId,
        String regionId
) implements Query<CartResult> {
}
