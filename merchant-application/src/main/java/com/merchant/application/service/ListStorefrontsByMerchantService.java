package com.merchant.application.service;

import com.merchant.application.port.inbound.ListStorefrontsByMerchantUseCase;

import com.merchant.application.model.write.StorefrontResult;
import com.merchant.application.model.read.ListStorefrontsByMerchantQuery;
import com.merchant.application.port.outbound.StorefrontQueryPort;
import com.merchant.application.model.read.StorefrontQueryCriteria;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListStorefrontsByMerchantService implements ListStorefrontsByMerchantUseCase {

    private final StorefrontQueryPort storefrontQueryPort;
    public List<StorefrontResult> execute(ListStorefrontsByMerchantQuery query) {
        return storefrontQueryPort.list(new StorefrontQueryCriteria(query.merchantId().getValue()))
                .stream()
                .map(StorefrontResult::from)
                .toList();
    }
}
