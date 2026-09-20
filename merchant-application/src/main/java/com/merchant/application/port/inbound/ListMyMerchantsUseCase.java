package com.merchant.application.port.inbound;

import com.merchant.application.model.read.ListMyMerchantsQuery;
import java.util.List;
import com.merchant.application.model.write.MerchantAccountResult;

public interface ListMyMerchantsUseCase {
    List<MerchantAccountResult> execute(ListMyMerchantsQuery query);
}
