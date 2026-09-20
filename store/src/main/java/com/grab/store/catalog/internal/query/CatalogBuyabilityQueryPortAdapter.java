package com.grab.store.catalog.internal.query;

import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.catalog.application.port.outbound.BuyabilityQueryPort;
import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogBuyabilityQueryPortAdapter implements CatalogBuyabilityQueryPort {

    private final BuyabilityQueryPort buyabilityQueryPort;

    @Override
    @CatalogReadTransactional
    public Optional<CatalogVariantSlice> findVariant(String variantId) {
        return buyabilityQueryPort.findVariant(variantId).map(this::toSlice);
    }

    @Override
    @CatalogReadTransactional
    public boolean isPublished(String variantId, String salesChannelId) {
        return buyabilityQueryPort.isPublished(variantId, salesChannelId);
    }

    @Override
    @CatalogReadTransactional
    public List<String> variantIdsForProduct(String productId) {
        return buyabilityQueryPort.variantIdsForProduct(productId);
    }

    @Override
    @CatalogReadTransactional
    public List<PublicationSlice> listPublications() {
        return buyabilityQueryPort.listPublications().stream()
                .map(p -> new PublicationSlice(p.variantId(), p.salesChannelId()))
                .toList();
    }

    @Override
    @CatalogReadTransactional
    public List<String> salesChannelIdsForVariant(String variantId) {
        return buyabilityQueryPort.salesChannelIdsForVariant(variantId);
    }

    private CatalogVariantSlice toSlice(BuyabilityQueryPort.VariantSlice slice) {
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
