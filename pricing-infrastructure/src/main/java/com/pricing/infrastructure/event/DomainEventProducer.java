package com.pricing.infrastructure.event;

import com.grab.framework.domain.Event;

import java.util.List;

public interface DomainEventProducer {
    void produce(Event event);
    void produce(List<Event> events);
}
