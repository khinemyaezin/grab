package com.grab.app.channel.discovery.internal.adapter;

import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.framework.id.Id;
import com.grab.app.catalog.catalog.port.CatalogDiscoveryQuery;
import com.grab.app.channel.discovery.internal.adapter.mapper.CatalogDiscoveryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogDiscoveryAdapter implements CatalogPort {
    private final CatalogDiscoveryQuery catalog;
    private final CatalogDiscoveryMapper mapper;
    @Override
    public Optional<ProductFacts> findProduct(Id productId) {
        Optional<CatalogDiscoveryQuery.ProductFacts> facts = catalog.findProduct(productId);
        return facts.map(mapper::toProductFacts);
    }

    @Override
    public Optional<VariantFacts> findVariant(Id variantId) {
        Optional<CatalogDiscoveryQuery.VariantFacts> facts = catalog.findVariant(variantId);
        return facts.map(mapper::toVariantFacts);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsAfter(
            Id productId,
            Id salesChannelId,
            int limit
    ) {
        List<CatalogDiscoveryQuery.ProductChannelPair> pairs = catalog.findPublishedProductChannelsAfter(
                productId,
                salesChannelId,
                limit
        );
        return mapper.toProductChannelPairs(pairs);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsBySku(String sku) {
        List<CatalogDiscoveryQuery.ProductChannelPair> pairs = catalog.findPublishedProductChannelsBySku(sku);
        return mapper.toProductChannelPairs(pairs);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsForSalesChannelAfter(
            Id salesChannelId,
            Id afterProductId,
            int limit
    ) {
        List<CatalogDiscoveryQuery.ProductChannelPair> pairs = catalog.findPublishedProductChannelsForSalesChannelAfter(
                salesChannelId,
                afterProductId,
                limit
        );
        return mapper.toProductChannelPairs(pairs);
    }

    @Override
    public boolean isPublishedInChannel(Id productId, Id salesChannelId) {
        return catalog.isPublishedInChannel(productId, salesChannelId);
    }
}
