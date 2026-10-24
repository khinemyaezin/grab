package com.grab.discovery.application.policy;

import com.grab.discovery.application.model.projection.DiscoveryProductProjection;
import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

public class DiscoveryProjectionPolicy {

    public boolean isOlderThanStored(Instant occurredAt, DiscoveryProductProjection stored) {
        return stored != null && occurredAt.isBefore(stored.updatedAt());
    }

    public boolean isInvalidated(DiscoveryProductProjection projection) {
        return !projection.published()
                && !projection.hasStock()
                && projection.slug() == null
                && projection.variants().isEmpty();
    }

    public DiscoveryProductProjection compose(
            Id salesChannelId,
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String productStatus,
            boolean channelEnabled,
            List<DiscoveryProductProjection.ProductOption> options,
            List<DiscoveryProductProjection.VariantFact> variants,
            Instant watermark,
            long version
    ) {
        boolean published = !variants.isEmpty();
        boolean hasStock = variants.stream().anyMatch(variant ->
                "ACTIVE".equals(variant.status()) && variant.inStock()
        );
        return new DiscoveryProductProjection(
                salesChannelId,
                productId,
                merchantId,
                categoryId,
                title,
                slug,
                description,
                thumbnail,
                productStatus,
                published,
                channelEnabled,
                hasStock,
                options,
                variants,
                watermark,
                version
        );
    }

    public DiscoveryProductProjection invalidate(DiscoveryProductProjection stored, Instant watermark) {
        Instant acceptedWatermark = watermark.isAfter(stored.updatedAt()) ? watermark : stored.updatedAt();
        return new DiscoveryProductProjection(
                stored.salesChannelId(),
                stored.productId(),
                stored.merchantId(),
                stored.categoryId(),
                stored.title(),
                null,
                stored.description(),
                stored.thumbnail(),
                stored.productStatus(),
                false,
                stored.channelEnabled(),
                false,
                stored.options(),
                List.of(),
                acceptedWatermark,
                stored.version()
        );
    }
}
