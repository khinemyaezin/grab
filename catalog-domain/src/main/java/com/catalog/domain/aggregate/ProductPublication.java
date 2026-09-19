package com.catalog.domain.aggregate;

import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class ProductPublication extends AggregateRoot<Id> {
    private final Id variantId;
    private final Id salesChannelId;
    private final Instant publishedAt;

    private ProductPublication(Id id, Id variantId, Id salesChannelId, Instant publishedAt) {
        super(id);
        this.variantId = Objects.requireNonNull(variantId, "variantId is required");
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        this.publishedAt = publishedAt;
    }

    public static ProductPublication publish(Id variantId, Id salesChannelId, Instant now) {
        ProductPublication publication = restore(variantId, salesChannelId, now);
        publication.addEvent(new ProductPublishedToChannelEvent(variantId, salesChannelId));
        return publication;
    }

    public static ProductPublication restore(Id variantId, Id salesChannelId, Instant publishedAt) {
        Id variant = Objects.requireNonNull(variantId, "variantId is required");
        Id channel = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        return new ProductPublication(identity(variant, channel), variant, channel, publishedAt);
    }

    public void unpublish() {
        addEvent(new ProductUnpublishedFromChannelEvent(variantId, salesChannelId));
    }

    public static Id identity(Id variantId, Id salesChannelId) {
        return new CommonId(variantId.getValue() + ":" + salesChannelId.getValue());
    }
}
