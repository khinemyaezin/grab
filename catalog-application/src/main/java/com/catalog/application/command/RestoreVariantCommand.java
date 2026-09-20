package com.catalog.application.command;

import com.grab.framework.id.Id;
import com.grab.framework.cqrs.command.Command;

public record RestoreVariantCommand(
        Id merchantId,
        Id productId,
        Id variantId
) implements Command<RestoreVariantResult> {}
