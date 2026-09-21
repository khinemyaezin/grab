package com.grab.store.catalog.internal.api.query;

import com.catalog.application.model.read.*;
import com.catalog.application.port.inbound.FindCatalogVariantSliceUseCase;
import com.catalog.application.port.inbound.FindPublishedVariantUseCase;
import com.catalog.application.port.inbound.ListCatalogPublicationsUseCase;
import com.catalog.application.port.inbound.ListSalesChannelIdsForVariantUseCase;
import com.catalog.application.port.inbound.ListVariantIdsForProductUseCase;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogBuyabilityQueryPortAdapter implements CatalogBuyabilityQueryPort {

    private final FindPublishedVariantUseCase findPublishedVariantUseCase;
    private final FindCatalogVariantSliceUseCase findCatalogVariantSliceUseCase;
    private final ListCatalogPublicationsUseCase listCatalogPublicationsUseCase;
    private final ListVariantIdsForProductUseCase listVariantIdsForProductUseCase;
    private final ListSalesChannelIdsForVariantUseCase listSalesChannelIdsForVariantUseCase;

    @Override
    @CatalogReadTransactional
    public Optional<CatalogVariantSlice> findPublished(String variantId, String salesChannelId) {
        return findPublishedVariantUseCase.execute(new FindPublishedVariantQuery(variantId, salesChannelId))
                .map(this::toSlice);
    }

    @Override
    @CatalogReadTransactional
    public Optional<CatalogVariantSlice> findVariant(String variantId) {
        return findCatalogVariantSliceUseCase.execute(new FindCatalogVariantSliceQuery(variantId))
                .map(this::toSlice);
    }

    @Override
    @CatalogReadTransactional
    public List<String> variantIdsForProduct(String productId) {
        return listVariantIdsForProductUseCase.execute(new ListVariantIdsForProductQuery(productId));
    }

    @Override
    @CatalogReadTransactional
    public List<PublicationSlice> listPublications() {
        return listCatalogPublicationsUseCase.execute(new ListCatalogPublicationsQuery()).stream()
                .map(item -> new PublicationSlice(item.variantId(), item.salesChannelId()))
                .toList();
    }

    @Override
    @CatalogReadTransactional
    public List<String> salesChannelIdsForVariant(String variantId) {
        return listSalesChannelIdsForVariantUseCase.execute(new ListSalesChannelIdsForVariantQuery(variantId));
    }

    private CatalogVariantSlice toSlice(FindCatalogVariantSliceResult slice) {
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

    private CatalogVariantSlice toSlice(FindPublishedVariantResult slice) {
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
