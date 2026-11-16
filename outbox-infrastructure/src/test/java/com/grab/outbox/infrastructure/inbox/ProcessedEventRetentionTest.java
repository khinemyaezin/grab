package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessedEventRetentionTest {

    private static final Instant NOW = Instant.parse("2026-11-13T10:00:00Z");

    private final ProcessedEventRepository repository = mock(ProcessedEventRepository.class);

    @Test
    void purge_deletesRowsOlderThanRetentionInBatchesUntilExhausted() {
        when(repository.deleteProcessedBefore(any(Instant.class), anyInt())).thenReturn(100, 100, 7);

        retention().purgeExpired();

        verify(repository, times(3)).deleteProcessedBefore(NOW.minus(Duration.ofDays(14)), 100);
    }

    private ProcessedEventRetention retention() {
        return new ProcessedEventRetention("catalog", repository, new NoOpTransactionManager(),
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
