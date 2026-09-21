package com.merchant.application.port.inbound;

import com.merchant.application.model.read.GetStorefrontQuery;
import com.merchant.application.model.write.StorefrontResult;

public interface GetStorefrontUseCase {
    StorefrontResult execute(GetStorefrontQuery query);
}
