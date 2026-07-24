package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;

public record CreatePricePreferenceCommand(
        String attribute,
        String value,
        boolean taxInclusive
) implements Command<PricePreferenceResult> {
}
