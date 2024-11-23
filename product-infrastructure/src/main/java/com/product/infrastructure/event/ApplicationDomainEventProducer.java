package com.product.infrastructure.event;

import com.product.domain.event.Event;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
