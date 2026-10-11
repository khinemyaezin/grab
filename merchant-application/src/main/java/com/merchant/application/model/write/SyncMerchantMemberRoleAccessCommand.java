package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

import java.util.Objects;
import java.util.Set;

public record SyncMerchantMemberRoleAccessCommand(
        Id merchantId,
        Id memberId,
        Id userId,
        String previousRole,
        String newRole,
        Set<String> authorities
) implements Command<Void> {
    public SyncMerchantMemberRoleAccessCommand {
        Objects.requireNonNull(merchantId, "merchantId is required");
        Objects.requireNonNull(memberId, "memberId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(newRole, "newRole is required");
        if (authorities == null) {
            authorities = Set.of();
        }
    }
}
