package com.grab.store.merchant.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ChangeStorefrontLifecycleCommand(
        Id storefrontId,
        Id merchantId,
        Action action,
        String reason
) implements Command<StorefrontResult> {
    public enum Action {
        ACTIVATE,
        SUSPEND,
        REACTIVATE,
        CLOSE
    }
}
