package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record SubmitMerchantApplicationCommand(
        Id merchantId,
        Id applicantUserId
) implements Command<MerchantAccountResult> {
}
