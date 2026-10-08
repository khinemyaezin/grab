package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.time.Instant;

public record ProvisionMerchantAdminCommand(
        Id merchantId,
        Id applicantUserId,
        Instant occurredAt
) implements Command<MerchantMemberResult> {
}
