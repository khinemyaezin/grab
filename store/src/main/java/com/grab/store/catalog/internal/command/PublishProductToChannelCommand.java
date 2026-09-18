package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record PublishProductToChannelCommand(
        Id merchantId,
        Id productId,
        Id variantId,
        Id salesChannelId
) implements Command<PublishProductToChannelResult> {
}
