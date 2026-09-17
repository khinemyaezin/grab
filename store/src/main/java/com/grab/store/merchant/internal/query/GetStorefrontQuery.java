package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.StorefrontResult;

public record GetStorefrontQuery(
        Id storefrontId,
        Id merchantId
) implements Query<StorefrontResult> {
}
