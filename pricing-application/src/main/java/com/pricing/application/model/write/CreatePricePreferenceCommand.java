package com.pricing.application.model.write;

import com.grab.framework.cqrs.command.Command;

public record CreatePricePreferenceCommand(
        String attribute,
        String value,
        boolean taxInclusive
) implements Command<PricePreferenceResult> {
}
