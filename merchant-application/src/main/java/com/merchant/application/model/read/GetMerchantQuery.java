package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.merchant.application.model.write.MerchantAccountResult;

public record GetMerchantQuery(
        Id merchantId,
        Id actorId,
        boolean reviewerAccess,
        boolean scopedAccess
) implements Query<MerchantAccountResult> {
}
