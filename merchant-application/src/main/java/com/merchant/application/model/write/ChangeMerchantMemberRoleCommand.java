package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.merchant.domain.valueobject.MerchantRole;

public record ChangeMerchantMemberRoleCommand(
        Id merchantId,
        Id memberId,
        Id actorId,
        MerchantRole newRole
) implements Command<MerchantMemberResult> {
}
