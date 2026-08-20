package com.grab.store.pricing.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record DeletePriceSetForDeletedVariantCommand(
        Id variantId
) implements Command<DeletePriceSetForDeletedVariantResult> {
}
