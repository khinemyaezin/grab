package com.grab.store.shared.workflow;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a module outbox with real transaction semantics: produced events are only visible after
 * the surrounding transaction commits, and are discarded when it rolls back. Tests use this to
 * prove a signal genuinely survives (or does not survive) a failing step.
 */
public final class FakeModuleOutbox {

    private final List<Event> pending = new ArrayList<>();
    private final List<Event> committed = new ArrayList<>();

    public DomainEventProducer producer() {
        return (aggregateType, aggregateId, events) -> pending.addAll(events);
    }

    public PlatformTransactionManager transactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                pending.clear();
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
                committed.addAll(pending);
                pending.clear();
            }

            @Override
            public void rollback(TransactionStatus status) {
                pending.clear();
            }
        };
    }

    public List<Event> committed() {
        return List.copyOf(committed);
    }
}
