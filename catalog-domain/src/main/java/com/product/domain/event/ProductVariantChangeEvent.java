package com.product.domain.event;

import com.grab.framework.domain.Event;

public record ProductVariantChangeEvent(
        String sku
) implements Event {

}