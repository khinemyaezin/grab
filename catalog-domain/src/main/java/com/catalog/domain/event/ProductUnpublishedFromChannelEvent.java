package com.catalog.domain.event;

import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;

public record ProductUnpublishedFromChannelEvent(
        Id variantId,
        Id salesChannelId
) implements Event {
}
