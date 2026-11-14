package com.grab.outbox.infrastructure.adapter.out.spring;

import com.grab.outbox.infrastructure.core.port.out.OutboxTransactions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public final class SpringOutboxTransactions implements OutboxTransactions {

    private final TransactionTemplate writes;
    private final TransactionTemplate reads;

    public SpringOutboxTransactions(PlatformTransactionManager transactionManager) {
        writes = new TransactionTemplate(transactionManager);
        writes.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        reads = new TransactionTemplate(transactionManager);
        reads.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        reads.setReadOnly(true);
    }

    @Override
    public <R> R inNewTransaction(Supplier<R> work) {
        return writes.execute(status -> work.get());
    }

    @Override
    public <R> R readOnly(Supplier<R> work) {
        return reads.execute(status -> work.get());
    }
}
