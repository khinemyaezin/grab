package com.grab.framework.outbox;

public interface InFlightTracker<ID> {
    boolean tryAcquire(ID id);

    void release(ID id);
}
