package com.catalog.application.service;

import com.catalog.application.model.read.FindPublishedVariantQuery;
import com.catalog.application.model.read.FindPublishedVariantResult;
import com.catalog.application.port.inbound.FindPublishedVariantUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindPublishedVariantService implements FindPublishedVariantUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public Optional<FindPublishedVariantResult> execute(FindPublishedVariantQuery query) {
        return buyabilityQueryPort.findPublished(query.variantId(), query.salesChannelId())
                .map(this::toResult);
    }

    private FindPublishedVariantResult toResult(BuyabilityQueryPort.VariantSlice slice) {
        return new FindPublishedVariantResult(
                slice.variantId(),
                slice.productId(),
                slice.sellerId(),
                slice.sku(),
                slice.title(),
                slice.slug(),
                slice.productStatus(),
                slice.untracked(),
                slice.media()
        );
    }
}
