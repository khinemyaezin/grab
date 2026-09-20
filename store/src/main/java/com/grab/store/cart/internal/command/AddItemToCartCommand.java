package com.grab.store.cart.internal.command;

import com.grab.framework.cqrs.command.Command;

import java.math.BigDecimal;

public record AddItemToCartCommand(
        String guestToken,
        String salesChannelId,
        String regionId,
        String currencyCode,
        String variantId,
        int quantity,
        BigDecimal displayedAmount
) implements Command<CartResult> {
}
