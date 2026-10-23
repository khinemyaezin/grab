package com.merchant.application.model.write;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;
import com.merchant.domain.enums.MerchantType;

public record StartMerchantApplicationCommand(
        Id applicantUserId,
        MerchantType type,
        String displayName,
        String primaryMarketCountryCode
) implements Command<MerchantAccountResult> {
}
