package com.grab.store.merchant.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record SubmitMerchantApplicationCommand(
        Id merchantId,
        Id applicantUserId
) implements Command<MerchantAccountResult> {
}
