package com.merchant.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.merchant.application.model.write.StorefrontResult;

public record GetStorefrontQuery(
        Id storefrontId,
        Id merchantId
) implements Query<StorefrontResult> {
}
