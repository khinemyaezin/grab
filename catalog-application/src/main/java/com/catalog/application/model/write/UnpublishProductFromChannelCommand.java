package com.catalog.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UnpublishProductFromChannelCommand(
        Id merchantId,
        Id productId,
        Id variantId,
        Id salesChannelId
) implements Command<UnpublishProductFromChannelResult> {
}
