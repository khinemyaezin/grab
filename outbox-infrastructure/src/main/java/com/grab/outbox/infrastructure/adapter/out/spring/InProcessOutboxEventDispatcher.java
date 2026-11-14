package com.grab.outbox.infrastructure.adapter.out.spring;

import com.grab.framework.domain.Event;
import com.grab.framework.outbox.OutboxEventDispatcher;
import org.springframework.context.ApplicationEventPublisher;

public record InProcessOutboxEventDispatcher(ApplicationEventPublisher publisher) implements OutboxEventDispatcher {

    @Override
    public void dispatch(Event event) {
        publisher.publishEvent(event);
    }
}
