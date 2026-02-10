package com.catalog.domain.event;

import com.grab.framework.id.Id;
import com.grab.framework.domain.Event;

public record CategoryChangedEvent(
        Id oldCategory,
        Id newCategory,
        Id productId
) implements Event {

}
