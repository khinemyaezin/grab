package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.merchant.application.model.write.StorefrontResult;

import java.util.List;

public record ListStorefrontsByMerchantQuery(
        Id merchantId
) implements Query<List<StorefrontResult>> {
}
