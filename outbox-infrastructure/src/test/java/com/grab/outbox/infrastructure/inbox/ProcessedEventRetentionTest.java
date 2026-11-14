package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessedEventRetentionTest {

    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    private final ProcessedEventRepository repository = mock(ProcessedEventRepository.class);

    @Test
    void purge_deletesRowsOlderThanRetentionInBatchesUntilExhausted() {
        when(repository.deleteProcessedBefore(any(Instant.class), anyInt())).thenReturn(100, 100, 7);

        retention(SingletonJobExecutor.local()).purgeExpired();

        verify(repository, times(3)).deleteProcessedBefore(NOW.minus(Duration.ofDays(14)), 100);
    }

    @Test
    void purge_skippedWhenAnotherReplicaHoldsTheLock() {
        AtomicReference<String> lockName = new AtomicReference<>();
        SingletonJobExecutor lockedElsewhere = (job, task) -> {
            lockName.set(job.name());
            return false;
        };

        retention(lockedElsewhere).purgeExpired();

        assertEquals("catalog.inbox.cleanup", lockName.get());
        verify(repository, never()).deleteProcessedBefore(any(), anyInt());
    }

    private ProcessedEventRetention retention(SingletonJobExecutor executor) {
        return new ProcessedEventRetention("catalog", repository, new NoOpTransactionManager(), executor,
                Duration.ofDays(14), 100, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class NoOpTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }
}
