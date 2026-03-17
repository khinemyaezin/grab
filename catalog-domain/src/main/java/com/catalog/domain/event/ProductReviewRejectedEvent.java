package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductReviewRejectedEvent(
        Id productId,
        String reason
) implements Event {
}
