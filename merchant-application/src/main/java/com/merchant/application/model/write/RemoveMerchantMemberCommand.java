package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record RemoveMerchantMemberCommand(
        Id merchantId,
        Id memberId,
        Id actorId
) implements Command<MerchantMemberResult> {
}
