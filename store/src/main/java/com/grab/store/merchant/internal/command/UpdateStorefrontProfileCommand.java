package com.grab.store.merchant.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateStorefrontProfileCommand(
        Id storefrontId,
        Id merchantId,
        String name,
        String slug
) implements Command<StorefrontResult> {
}
