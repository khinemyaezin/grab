package com.grab.framework.outbox;

import com.grab.framework.domain.Event;

public interface OutboxEventDispatcher {
    void dispatch(Event event);
}
