package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ChangeMerchantLifecycleCommand(
        Id merchantId,
        Id actorId,
        Action action,
        String reason
) implements Command<MerchantAccountResult> {
    public enum Action {
        REQUEST_CHANGES,
        APPROVE,
        REJECT,
        SUSPEND,
        REACTIVATE,
        CLOSE
    }
}
