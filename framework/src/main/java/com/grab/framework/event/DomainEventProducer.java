package com.grab.framework.event;

import com.grab.framework.domain.Event;

import java.util.List;

public interface DomainEventProducer {
    void produce(String aggregateType, String aggregateId, List<Event> events);
}
