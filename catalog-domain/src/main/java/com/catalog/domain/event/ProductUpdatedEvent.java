package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductUpdatedEvent(
        Id productId,
        String newName,
        Id newCategoryId
) implements Event {

}
