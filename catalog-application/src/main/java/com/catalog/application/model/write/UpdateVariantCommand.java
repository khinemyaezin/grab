package com.catalog.application.model.write;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record UpdateVariantCommand(
        Id merchantId,
        Id productId,
        Id variantId,
        String sku,
        Boolean manageInventory
) implements Command<UpdateVariantResult> {}
