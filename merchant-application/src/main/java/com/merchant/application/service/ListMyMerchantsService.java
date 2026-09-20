package com.merchant.application.service;

import com.merchant.application.model.write.MerchantAccountResult;
import com.merchant.application.port.inbound.ListMyMerchantsUseCase;
import com.merchant.application.port.outbound.MerchantAccountQueryPort;
import com.merchant.application.model.read.ListMyMerchantsQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListMyMerchantsService implements ListMyMerchantsUseCase {
    private final MerchantAccountQueryPort merchants;

    public List<MerchantAccountResult> execute(ListMyMerchantsQuery query) {
        return merchants.findByApplicantUserId(query.applicantUserId().getValue()).stream()
                .map(MerchantAccountResult::from)
                .toList();
    }
}
