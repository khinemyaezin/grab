package com.merchant.application.port.inbound;

import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import java.util.List;
import com.merchant.application.model.write.MerchantAccountResult;

public interface ListMerchantReviewQueueUseCase {
    List<MerchantAccountResult> execute(ListMerchantReviewQueueQuery query);
}
