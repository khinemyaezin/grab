package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record ReplacePriceListRulesCommand(
        Id priceListId,
        List<PriceListRuleInput> rules
) implements Command<PriceListResult> {
}
