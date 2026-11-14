package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Class B job deleting inbox rows older than the retention window (outbox retention plus a safety margin), in
 * bounded batches with one short transaction per batch. Registered as one bean per consuming schema; the lock name
 * is {@code <module>.inbox.cleanup}.
 */
public final class ProcessedEventRetention {

    public static final Duration DEFAULT_RETENTION = Duration.ofDays(14);
    public static final int DEFAULT_BATCH_SIZE = 1000;

    private static final Logger log = Loggers.getLogger(ProcessedEventRetention.class);

    private final String module;
    private final ProcessedEventRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final Duration retention;
    private final int batchSize;
    private final Clock clock;

    public ProcessedEventRetention(String module, ProcessedEventRepository repository,
                                   PlatformTransactionManager transactionManager, Duration retention, int batchSize) {
        this(module, repository, transactionManager, retention, batchSize, Clock.systemUTC());
    }

    public ProcessedEventRetention(
            String module,
            ProcessedEventRepository repository,
            PlatformTransactionManager transactionManager,
            Duration retention,
            int batchSize,
            Clock clock
    ) {
        this.module = Objects.requireNonNull(module, "module");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.retention = retention == null ? DEFAULT_RETENTION : retention;
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public void purgeExpired() {
        Instant cutoff = clock.instant().minus(retention);
        int total = 0;
        int deleted;
        do {
            Integer batch = transactionTemplate.execute(status -> repository.deleteProcessedBefore(cutoff, batchSize));
            deleted = batch == null ? 0 : batch;
            total += deleted;
        } while (deleted >= batchSize);
        if (total > 0) {
            log.info("Purged {} processed-event inbox rows module={} cutoff={}", total, module, cutoff);
        }
    }
}
