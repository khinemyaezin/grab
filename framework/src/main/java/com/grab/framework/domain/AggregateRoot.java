package com.grab.framework.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class AggregateRoot<ID extends Serializable> extends Entity<ID> {
    private final List<Event> events = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    protected void addEvent(Event event) {
        events.add(event);
    }

    public List<Event> pullEvents() {
        List<Event> snapshot = List.copyOf(events);
        events.clear();
        return snapshot;
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
