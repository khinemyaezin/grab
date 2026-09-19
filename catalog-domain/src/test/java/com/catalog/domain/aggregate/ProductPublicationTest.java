package com.catalog.domain.aggregate;

import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPublicationTest {

    @Test
    void publish_emitsPublishedEventAndUsesVariantNaturalKey() {
        var variantId = new CommonId("var-1");
        var channelId = new CommonId("channel-1");
        Instant publishedAt = Instant.parse("2026-09-17T00:00:00Z");

        ProductPublication publication = ProductPublication.publish(variantId, channelId, publishedAt);

        assertThat(publication.getId().getValue()).isEqualTo("var-1:channel-1");
        assertThat(publication.getVariantId()).isEqualTo(variantId);
        assertThat(publication.getSalesChannelId()).isEqualTo(channelId);
        assertThat(publication.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(publication.getEvents()).hasSize(1);
        assertThat(publication.getEvents().getFirst()).isInstanceOf(ProductPublishedToChannelEvent.class);
        ProductPublishedToChannelEvent event = (ProductPublishedToChannelEvent) publication.getEvents().getFirst();
        assertThat(event.variantId()).isEqualTo(variantId);
        assertThat(event.salesChannelId()).isEqualTo(channelId);
    }

    @Test
    void restore_doesNotEmitEvents() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("var-1"),
                new CommonId("channel-1"),
                Instant.parse("2026-09-17T00:00:00Z")
        );

        assertThat(publication.getEvents()).isEmpty();
    }

    @Test
    void unpublish_emitsUnpublishedEvent() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("var-1"),
                new CommonId("channel-1"),
                Instant.parse("2026-09-17T00:00:00Z")
        );

        publication.unpublish();

        assertThat(publication.getEvents()).hasSize(1);
        assertThat(publication.getEvents().getFirst()).isInstanceOf(ProductUnpublishedFromChannelEvent.class);
        ProductUnpublishedFromChannelEvent event =
                (ProductUnpublishedFromChannelEvent) publication.getEvents().getFirst();
        assertThat(event.variantId().getValue()).isEqualTo("var-1");
        assertThat(event.salesChannelId().getValue()).isEqualTo("channel-1");
    }
}
