package com.cart.application.model.write;

import com.cart.application.model.read.CartResult;
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
