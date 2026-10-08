package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.merchant.domain.valueobject.MerchantRole;

import java.time.Instant;

public record InviteMerchantMemberCommand(
        Id merchantId,
        Id actorId,
        Id targetUserId,
        MerchantRole role,
        Instant expiresAt
) implements Command<MerchantMemberResult> {
}
