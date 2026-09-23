package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record AcceptMerchantMemberInvitationCommand(
        Id merchantId,
        Id memberId,
        Id actorUserId
) implements Command<MerchantMemberResult> {
}
