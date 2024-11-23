package com.product.infrastructure.event;

import com.product.domain.event.Event;

import java.util.List;

public interface DomainEventProducer {
    void produce(Event event);
    void produce(List<Event> events);
}
