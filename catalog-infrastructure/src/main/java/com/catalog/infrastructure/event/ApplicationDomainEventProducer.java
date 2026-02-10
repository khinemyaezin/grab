package com.catalog.infrastructure.event;

import com.grab.framework.domain.Event;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@AllArgsConstructor
public class ApplicationDomainEventProducer implements DomainEventProducer {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void produce(Event event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void produce(List<Event> events) {
        for (Event e: events) {
            applicationEventPublisher.publishEvent(e);
        }
    }
}
