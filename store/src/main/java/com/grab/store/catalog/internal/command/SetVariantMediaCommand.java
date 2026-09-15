package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.List;

public record SetVariantMediaCommand(
        Id merchantId,
        Id productId,
        Id variantId,
        List<Id> mediaIds,
        Id thumbnailMediaId
) implements Command<SetVariantMediaResult> {
}
