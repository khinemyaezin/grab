package com.grab.store.catalog.internal.api.adapter.mapper;

import com.catalog.application.model.read.FindCatalogVariantSliceResult;
import com.catalog.application.model.read.FindPublishedVariantResult;
import com.grab.store.catalog.port.CatalogBuyabilityQuery.CatalogVariantSlice;
import org.springframework.stereotype.Component;

@Component
public class CatalogBuyabilityQueryMapper {

    public CatalogVariantSlice toSlice(FindCatalogVariantSliceResult slice) {
        return new CatalogVariantSlice(
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

    public CatalogVariantSlice toSlice(FindPublishedVariantResult slice) {
        return new CatalogVariantSlice(
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
