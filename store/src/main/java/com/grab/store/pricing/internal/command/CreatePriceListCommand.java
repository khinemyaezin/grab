package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;

import java.time.Instant;

public record CreatePriceListCommand(
        String title,
        String description,
        String type,
        Instant startsAt,
        Instant endsAt
) implements Command<PriceListResult> {
}
