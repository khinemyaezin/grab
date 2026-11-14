package com.merchant.application.model.write;

import java.util.UUID;
import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record ChangeMerchantLifecycleCommand(
        Id merchantId,
        Id actorId,
        Action action,
        String reason,
        UUID sourceEventId
) implements Command<MerchantAccountResult> {

    /**
     * Command not triggered by an outbox delivery; no inbox de-duplication applies.
     */
    public ChangeMerchantLifecycleCommand(Id merchantId, Id actorId, Action action, String reason) {
        this(merchantId, actorId, action, reason, null);
    }

    public enum Action {
        REQUEST_CHANGES,
        APPROVE,
        REJECT,
        SUSPEND,
        REACTIVATE,
        CLOSE
    }
}
