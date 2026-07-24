package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RemovePriceFromPriceListCommand(
        Id priceListId,
        Id priceId
) implements Command<PriceListResult> {
}
