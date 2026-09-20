package com.merchant.application.port.inbound;

import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import java.util.List;
import com.merchant.application.model.write.StorefrontResult;

public interface ListStorefrontsByMerchantUseCase {
    List<StorefrontResult> execute(ListStorefrontsByMerchantQuery query);
}
