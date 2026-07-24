package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;

public record AddPriceToPriceListCommand(
        Id priceListId,
        Id priceSetId,
        String title,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        List<PriceRuleInput> rules
) implements Command<PriceListResult> {
}
