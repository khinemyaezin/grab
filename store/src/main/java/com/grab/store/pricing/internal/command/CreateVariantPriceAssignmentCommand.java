package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;

import java.math.BigDecimal;
import java.util.List;

public record CreateVariantPriceAssignmentCommand(
        String variantId,
        String productId,
        String sku,
        String merchantId,
        String title,
        String currencyCode,
        BigDecimal amount,
        Integer minQuantity,
        Integer maxQuantity,
        List<PriceRuleInput> rules
) implements Command<CreateVariantPriceAssignmentResult> {
}
