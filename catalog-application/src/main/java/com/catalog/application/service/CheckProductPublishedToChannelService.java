package com.catalog.application.service;

import com.catalog.application.model.read.CheckProductPublishedToChannelQuery;
import com.catalog.application.port.inbound.CheckProductPublishedToChannelUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CheckProductPublishedToChannelService implements CheckProductPublishedToChannelUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public boolean execute(CheckProductPublishedToChannelQuery query) {
        List<String> variantIds = buyabilityQueryPort.variantIdsForProduct(query.productId());
        for (String variantId : variantIds) {
            if (buyabilityQueryPort.isPublished(variantId, query.salesChannelId())) {
                return true;
            }
        }
        return false;
    }
}
