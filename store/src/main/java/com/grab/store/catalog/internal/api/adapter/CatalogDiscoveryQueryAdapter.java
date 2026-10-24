package com.grab.store.catalog.internal.api.adapter;

import com.catalog.application.model.read.CheckProductPublishedToChannelQuery;
import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.port.inbound.CheckProductPublishedToChannelUseCase;
import com.catalog.application.port.inbound.GetCatalogProductFactsUseCase;
import com.catalog.application.port.inbound.GetCatalogVariantFactsUseCase;
import com.catalog.application.port.inbound.ListPublishedProductChannelsAfterUseCase;
import com.grab.framework.id.Id;
import com.grab.store.catalog.internal.api.adapter.mapper.CatalogDiscoveryQueryMapper;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.port.CatalogDiscoveryQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogDiscoveryQueryAdapter implements CatalogDiscoveryQuery {

    private final GetCatalogProductFactsUseCase getCatalogProductFactsUseCase;
    private final GetCatalogVariantFactsUseCase getCatalogVariantFactsUseCase;
    private final ListPublishedProductChannelsAfterUseCase listPublishedProductChannelsAfterUseCase;
    private final CheckProductPublishedToChannelUseCase checkProductPublishedToChannelUseCase;
    private final CatalogDiscoveryQueryMapper mapper;

    @Override
    @CatalogReadTransactional
    public Optional<ProductFacts> findProduct(Id productId) {
        GetCatalogProductFactsQuery query = new GetCatalogProductFactsQuery(productId.getValue());
        return getCatalogProductFactsUseCase.execute(query)
                .map(result -> mapper.toFacts(result.productId(), result.merchantId(), result.product()));
    }

    @Override
    @CatalogReadTransactional
    public Optional<VariantFacts> findVariant(Id variantId) {
        String variantIdValue = variantId.getValue();
        GetCatalogVariantFactsQuery query = new GetCatalogVariantFactsQuery(variantIdValue);
        return getCatalogVariantFactsUseCase.execute(query)
                .flatMap(result -> mapper.toFacts(result.productId(), result.merchantId(), result.product())
                        .variants().stream()
                        .filter(v -> v.variantId().getValue().equals(variantIdValue))
                        .findFirst());
    }

    @Override
    @CatalogReadTransactional
    public List<ProductChannelPair> findPublishedProductChannelsAfter(
            Id afterProductId,
            Id afterSalesChannelId,
            int limit
    ) {
        ListPublishedProductChannelsAfterQuery query = new ListPublishedProductChannelsAfterQuery(
                afterProductId,
                afterSalesChannelId,
                limit
        );
        return mapper.toProductChannelPairsFromCatalog(listPublishedProductChannelsAfterUseCase.execute(query).items());
    }

    @Override
    @CatalogReadTransactional
    public boolean isPublishedInChannel(Id productId, Id salesChannelId) {
        CheckProductPublishedToChannelQuery query = new CheckProductPublishedToChannelQuery(
                productId.getValue(),
                salesChannelId.getValue()
        );
        return checkProductPublishedToChannelUseCase.execute(query);
    }
}
