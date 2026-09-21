package com.merchant.application.port.inbound;

import com.merchant.application.model.read.GetMerchantQuery;
import com.merchant.application.model.write.MerchantAccountResult;

public interface GetMerchantUseCase {
    MerchantAccountResult execute(GetMerchantQuery query);
}
