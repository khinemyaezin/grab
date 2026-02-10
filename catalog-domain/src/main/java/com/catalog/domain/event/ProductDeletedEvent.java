package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

import java.util.List;

public record ProductDeletedEvent(
        Id productId,
        Id categoryId,
        List<Id> variantIds
) implements Event {

}
