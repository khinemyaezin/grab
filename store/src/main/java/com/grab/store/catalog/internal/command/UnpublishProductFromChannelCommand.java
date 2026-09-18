package com.grab.store.catalog.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UnpublishProductFromChannelCommand(
        Id merchantId,
        Id productId,
        Id salesChannelId
) implements Command<UnpublishProductFromChannelResult> {
}
