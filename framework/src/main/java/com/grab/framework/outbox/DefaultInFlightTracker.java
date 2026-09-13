package com.grab.framework.outbox;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultInFlightTracker<ID> implements InFlightTracker<ID> {

    private final Set<ID> inFlight = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryAcquire(ID id) {
        Objects.requireNonNull(id, "id");
        return inFlight.add(id);
    }

    @Override
    public void release(ID id) {
        if (id != null) {
            inFlight.remove(id);
        }
    }
}
