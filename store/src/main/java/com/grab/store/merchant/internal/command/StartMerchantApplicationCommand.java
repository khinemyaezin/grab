package com.grab.store.merchant.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.MerchantType;

public record StartMerchantApplicationCommand(
        Id applicantUserId,
        MerchantType type,
        String displayName
) implements Command<MerchantAccountResult> {
}
