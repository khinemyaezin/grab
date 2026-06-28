package com.grab.store.merchant.internal.command;

import com.grab.framework.cqrs.command.Command;
import com.grab.framework.id.Id;

public record UpdateMerchantProfileCommand(
        Id merchantId,
        Id applicantUserId,
        String legalName,
        String displayName,
        String registrationCountryCode,
        String registrationNumber,
        String contactEmail,
        String contactPhone,
        String addressLine1,
        String addressLine2,
        String addressCity,
        String addressRegion,
        String addressPostalCode,
        String addressCountryCode
) implements Command<MerchantAccountResult> {
}
