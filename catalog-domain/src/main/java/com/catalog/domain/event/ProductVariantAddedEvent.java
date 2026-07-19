package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductVariantAddedEvent(
        Id productId,
        Id variantId,
        String sku,
        String productName
) implements Event {

}
