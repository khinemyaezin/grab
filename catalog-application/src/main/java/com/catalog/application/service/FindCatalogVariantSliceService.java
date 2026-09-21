package com.catalog.application.service;

import com.catalog.application.model.read.FindCatalogVariantSliceQuery;
import com.catalog.application.model.read.FindCatalogVariantSliceResult;
import com.catalog.application.port.inbound.FindCatalogVariantSliceUseCase;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindCatalogVariantSliceService implements FindCatalogVariantSliceUseCase {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    public Optional<FindCatalogVariantSliceResult> execute(FindCatalogVariantSliceQuery query) {
        return buyabilityQueryPort.findVariant(query.variantId()).map(this::toResult);
    }

    private FindCatalogVariantSliceResult toResult(BuyabilityQueryPort.VariantSlice slice) {
        return new FindCatalogVariantSliceResult(
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
