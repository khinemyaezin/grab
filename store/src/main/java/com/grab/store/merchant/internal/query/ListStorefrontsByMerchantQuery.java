package com.grab.store.merchant.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.merchant.internal.command.StorefrontResult;

import java.util.List;

public record ListStorefrontsByMerchantQuery(
        Id merchantId
) implements Query<List<StorefrontResult>> {
}
