package com.inventory.infrastructure.event;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@AllArgsConstructor
public class ApplicationDomainEventProducer implements DomainEventProducer {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void produce(String aggregateType, String aggregateId, List<Event> events) {
        for (Event e: events) {
            applicationEventPublisher.publishEvent(e);
        }
    }
}
