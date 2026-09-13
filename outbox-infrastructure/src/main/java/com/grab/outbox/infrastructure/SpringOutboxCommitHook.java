package com.grab.outbox.infrastructure;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class SpringOutboxCommitHook {

    private static final Logger log = Loggers.getLogger(SpringOutboxCommitHook.class);

    private SpringOutboxCommitHook() {
    }

    public static void afterCommit(Runnable action) {
        if (action == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("No transaction synchronization active; skipping afterCommit hot enqueue");
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    action.run();
                } catch (RuntimeException exception) {
                    log.warn("Outbox afterCommit hook failed; poller will recover", exception);
                }
            }
        });
    }
}
