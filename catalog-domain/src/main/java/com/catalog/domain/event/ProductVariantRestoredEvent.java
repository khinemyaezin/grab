package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductVariantRestoredEvent(
        Id productId,
        Id variantId
) implements Event {

}
