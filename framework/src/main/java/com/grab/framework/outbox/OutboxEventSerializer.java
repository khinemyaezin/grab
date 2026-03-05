package com.grab.framework.outbox;

import com.grab.framework.domain.Event;

public interface OutboxEventSerializer {
    SerializedEvent serialize(Event event);

    Event deserialize(SerializedEvent serializedEvent);
}
