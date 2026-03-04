package com.grab.framework.event;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.domain.Event;

import java.util.List;

public interface DomainEventProducer {
    default void produce(Event event) {
        produce("unknown", "unknown", List.of(event));
    }

    default void produce(List<Event> events) {
        produce("unknown", "unknown", events);
    }

    default void produce(AggregateRoot<?> aggregate) {
        produce(
                aggregate.getClass().getSimpleName(),
                String.valueOf(aggregate.getId()),
                aggregate.pullEvents()
        );
    }

    void produce(String aggregateType, String aggregateId, List<Event> events);
}
