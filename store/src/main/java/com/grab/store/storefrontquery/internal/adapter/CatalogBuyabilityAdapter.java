package com.grab.store.storefrontquery.internal.adapter;

import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.catalog.port.CatalogBuyabilityQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("storeFrontQueryCatalogBuyabilityAdapter")
@RequiredArgsConstructor
public class CatalogBuyabilityAdapter implements CatalogPort {
    private final CatalogBuyabilityQuery catalog;
    private final IdGenerator idGenerator;

    @Override
    public Optional<CatalogVariant> findVariant(Id variantId) {
        String variantValue = variantId.getValue();
        Optional<CatalogBuyabilityQuery.CatalogVariantSlice> sliceOpt = catalog.findVariant(variantValue);
        return sliceOpt.map(this::toCatalogVariant);
    }

    @Override
    public List<Id> variantIdsForProduct(Id productId) {
        String productValue = productId.getValue();
        List<String> variantValues = catalog.variantIdsForProduct(productValue);
        return variantValues.stream()
                .map(idGenerator::convertIdFrom)
                .toList();
    }

    @Override
    public List<Publication> listPublications() {
        List<CatalogBuyabilityQuery.PublicationSlice> publications = catalog.listPublications();
        return publications.stream()
                .map(this::toPublication)
                .toList();
    }

    @Override
    public List<Id> salesChannelIdsForVariant(Id variantId) {
        String variantValue = variantId.getValue();
        List<String> channelValues = catalog.salesChannelIdsForVariant(variantValue);
        return channelValues.stream()
                .map(idGenerator::convertIdFrom)
                .toList();
    }

    private CatalogVariant toCatalogVariant(CatalogBuyabilityQuery.CatalogVariantSlice slice) {
        Id variantId = idGenerator.convertIdFrom(slice.variantId());
        Id productId = idGenerator.convertIdFrom(slice.productId());
        Id sellerId = id(slice.sellerId());
        return new CatalogVariant(
                variantId,
                productId,
                sellerId,
                slice.sku(),
                slice.title(),
                slice.slug(),
                slice.productStatus(),
                slice.untracked(),
                slice.media()
        );
    }

    private Publication toPublication(CatalogBuyabilityQuery.PublicationSlice slice) {
        Id variantId = idGenerator.convertIdFrom(slice.variantId());
        Id salesChannelId = idGenerator.convertIdFrom(slice.salesChannelId());
        return new Publication(variantId, salesChannelId);
    }

    private Id id(String value) {
        return value == null ? null : idGenerator.convertIdFrom(value);
    }
}
