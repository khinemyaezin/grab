package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;

public record UpdatePriceListCommand(
        Id priceListId,
        String title,
        String description,
        String status,
        String type,
        Instant startsAt,
        Instant endsAt
) implements Command<PriceListResult> {
}
