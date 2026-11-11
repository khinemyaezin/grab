package com.catalog.application.port.outbound;

import com.catalog.domain.event.CategoryChangedEvent;
import com.catalog.domain.event.ProductDeletedEvent;
import com.catalog.domain.event.ProductMediaChangedEvent;
import com.catalog.domain.event.ProductPublishedToChannelEvent;
import com.catalog.domain.event.ProductStatusChangedEvent;
import com.catalog.domain.event.ProductUnpublishedFromChannelEvent;
import com.catalog.domain.event.ProductUpdatedEvent;
import com.catalog.domain.event.ProductVariantAddedEvent;
import com.catalog.domain.event.ProductVariantChangeEvent;
import com.catalog.domain.event.ProductVariantDeletedEvent;
import com.catalog.domain.event.ProductVariantRestoredEvent;
import com.catalog.domain.event.VariantMediaChangedEvent;

public interface CatalogIntegrationEventPublisher {

    void publishProductVariantAdded(ProductVariantAddedEvent event);

    void publishProductVariantChanged(ProductVariantChangeEvent event);

    void publishProductVariantDeleted(ProductVariantDeletedEvent event);

    void publishProductVariantRestored(ProductVariantRestoredEvent event);

    void publishProductUpdated(ProductUpdatedEvent event);

    void publishProductMediaChanged(ProductMediaChangedEvent event);

    void publishCategoryChanged(CategoryChangedEvent event);

    void publishVariantMediaChanged(VariantMediaChangedEvent event);

    void publishProductDeleted(ProductDeletedEvent event);

    void publishProductPublishedToChannel(ProductPublishedToChannelEvent event);

    void publishProductUnpublishedFromChannel(ProductUnpublishedFromChannelEvent event);

    void publishProductStatusChanged(ProductStatusChangedEvent event);
}

