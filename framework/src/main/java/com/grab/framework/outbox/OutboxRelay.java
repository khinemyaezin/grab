package com.grab.framework.outbox;

import java.util.List;

public interface OutboxRelay<ID> {

    boolean enqueueHot(ID id);

    boolean enqueueCold(ClaimedOutboxEvent<ID> claimed);

    int coldQueueRemainingCapacity();

    boolean isActive();

    void setHandler(OutboxWorkHandler<ID> handler);

    void start();

    void stop();

    /**
     * Removes and returns claimed cold work that was queued but never handed to a worker. Called after
     * {@link #stop()} so the processor can release those claims instead of leaving them PROCESSING until
     * the claim timeout expires.
     */
    default List<ClaimedOutboxEvent<ID>> drainCold() {
        return List.of();
    }

    @SuppressWarnings("unchecked")
    static <ID> OutboxRelay<ID> noop() {
        return (OutboxRelay<ID>) NoOp.INSTANCE;
    }

    final class NoOp implements OutboxRelay<Object> {
        private static final NoOp INSTANCE = new NoOp();

        private NoOp() {
        }

        @Override
        public boolean enqueueHot(Object id) {
            return false;
        }

        @Override
        public boolean enqueueCold(ClaimedOutboxEvent<Object> claimed) {
            return false;
        }

        @Override
        public int coldQueueRemainingCapacity() {
            return 0;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void setHandler(OutboxWorkHandler<Object> handler) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }
}
