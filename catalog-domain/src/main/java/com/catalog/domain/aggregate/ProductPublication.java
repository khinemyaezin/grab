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
    private final Id productId;
    private final Id salesChannelId;
    private final Instant publishedAt;

    private ProductPublication(Id id, Id productId, Id salesChannelId, Instant publishedAt) {
        super(id);
        this.productId = Objects.requireNonNull(productId, "productId is required");
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        this.publishedAt = publishedAt;
    }

    public static ProductPublication publish(Id productId, Id salesChannelId, Instant now) {
        ProductPublication publication = restore(productId, salesChannelId, now);
        publication.addEvent(new ProductPublishedToChannelEvent(productId, salesChannelId));
        return publication;
    }

    public static ProductPublication restore(Id productId, Id salesChannelId, Instant publishedAt) {
        Id product = Objects.requireNonNull(productId, "productId is required");
        Id channel = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        return new ProductPublication(identity(product, channel), product, channel, publishedAt);
    }

    public void unpublish() {
        addEvent(new ProductUnpublishedFromChannelEvent(productId, salesChannelId));
    }

    public static Id identity(Id productId, Id salesChannelId) {
        return new CommonId(productId.getValue() + ":" + salesChannelId.getValue());
    }
}
