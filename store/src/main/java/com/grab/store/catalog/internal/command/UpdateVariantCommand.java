package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;
public record UpdateVariantCommand(
        Id productId,
        Id variantId,
        String sku
) implements Command<UpdateVariantResult> {}
