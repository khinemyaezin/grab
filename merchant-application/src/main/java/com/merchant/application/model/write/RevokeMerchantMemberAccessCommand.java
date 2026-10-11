package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.Objects;

public record RevokeMerchantMemberAccessCommand(
        Id merchantId,
        Id memberId,
        Id userId,
        String role
) implements Command<Void> {
    public RevokeMerchantMemberAccessCommand {
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(memberId, "memberId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
    }
}
