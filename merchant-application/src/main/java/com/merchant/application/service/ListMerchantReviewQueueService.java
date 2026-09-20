package com.merchant.application.service;

import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMerchantReviewQueueUseCase;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.ListMerchantReviewQueueQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMerchantReviewQueueService implements ListMerchantReviewQueueUseCase {
    private final MerchantAccountQueryPort merchants;

    public List<MerchantAccountResult> execute(ListMerchantReviewQueueQuery query) {
        return merchants.findByStatus(query.status()).stream()
                .map(MerchantAccountResult::from)
                .toList();
    }
}
