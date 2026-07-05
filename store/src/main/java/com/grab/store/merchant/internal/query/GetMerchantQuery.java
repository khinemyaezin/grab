package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.MerchantAccountResult;

public record GetMerchantQuery(
        Id merchantId,
        Id actorId,
        boolean reviewerAccess,
        boolean scopedAccess
) implements Query<MerchantAccountResult> {
}
