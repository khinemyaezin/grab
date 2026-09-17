package com.catalog.domain.aggregate;

import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPublicationTest {

    @Test
    void publish_emitsPublishedEventAndUsesNaturalKey() {
        var productId = new CommonId("p1");
        var channelId = new CommonId("channel-1");
        Instant publishedAt = Instant.parse("2026-09-17T00:00:00Z");

        ProductPublication publication = ProductPublication.publish(productId, channelId, publishedAt);

        assertThat(publication.getId().getValue()).isEqualTo("p1:channel-1");
        assertThat(publication.getProductId()).isEqualTo(productId);
        assertThat(publication.getSalesChannelId()).isEqualTo(channelId);
        assertThat(publication.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(publication.getEvents()).hasSize(1);
        assertThat(publication.getEvents().getFirst()).isInstanceOf(ProductPublishedToChannelEvent.class);
    }

    @Test
    void restore_doesNotEmitEvents() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("p1"),
                new CommonId("channel-1"),
                Instant.parse("2026-09-17T00:00:00Z")
        );

        assertThat(publication.getEvents()).isEmpty();
    }

    @Test
    void unpublish_emitsUnpublishedEvent() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("p1"),
                new CommonId("channel-1"),
                Instant.parse("2026-09-17T00:00:00Z")
        );

        publication.unpublish();

        assertThat(publication.getEvents()).hasSize(1);
        assertThat(publication.getEvents().getFirst()).isInstanceOf(ProductUnpublishedFromChannelEvent.class);
    }
}
