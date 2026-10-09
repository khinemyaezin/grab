package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.AccessProvisioningStatus;

import java.time.Instant;
import java.util.Objects;

public record RecordMemberProvisioningResultCommand(
        Id merchantId,
        Id userId,
        AccessProvisioningStatus outcome,
        String errorCode,
        long memberVersion,
        Instant completedAt
) implements Command<MerchantMemberResult> {
    public RecordMemberProvisioningResultCommand {
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(outcome, "outcome is required");
        if (completedAt == null) {
            completedAt = Instant.now();
        }
    }
}
