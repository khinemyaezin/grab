package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdatePricePreferenceCommand(
        Id pricePreferenceId,
        String attribute,
        String value,
        boolean taxInclusive
) implements Command<PricePreferenceResult> {
}
