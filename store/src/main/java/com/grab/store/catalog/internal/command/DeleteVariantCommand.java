package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.cqrs.command.Command;

public record DeleteVariantCommand(
        Id productId,
        Id variantId
) implements Command<DeleteVariantResult> {}
