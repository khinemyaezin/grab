package com.grab.framework.outbox;

public interface OutboxRelay<ID> {

    boolean enqueueHot(ID id);

    boolean enqueueCold(ClaimedOutboxEvent<ID> claimed);

    int coldQueueRemainingCapacity();

    boolean isActive();

    void setHandler(OutboxWorkHandler<ID> handler);

    void start();

    void stop();

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
