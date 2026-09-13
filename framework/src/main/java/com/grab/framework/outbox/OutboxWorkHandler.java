package com.grab.framework.outbox;

@FunctionalInterface
public interface OutboxWorkHandler<ID> {
    void handle(QueuedOutboxWork<ID> work);
}
