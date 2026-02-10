package com.catalog.domain.event;

import com.grab.framework.domain.Event;

public record ProductVariantChangeEvent(
        String sku
) implements Event {

}