package com.product.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.util.List;

public record ProductVariantDeletedEvent(
        Id productId,
        Id categoryId,
        Id variantId
) implements Event {

}
