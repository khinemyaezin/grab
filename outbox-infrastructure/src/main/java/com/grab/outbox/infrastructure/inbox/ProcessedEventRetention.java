package com.grab.outbox.infrastructure.inbox;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.scheduling.ClusterSingletonJob;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.springframework.scheduling.annotation.Scheduled;
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
public class ProcessedEventRetention {

    public static final Duration DEFAULT_RETENTION = Duration.ofDays(14);
    public static final int DEFAULT_BATCH_SIZE = 1000;

    private static final Logger log = Loggers.getLogger(ProcessedEventRetention.class);

    private final String module;
    private final ProcessedEventRepository repository;
    private final TransactionTemplate transactionTemplate;
    private final SingletonJobExecutor singletonJobExecutor;
    private final SingletonJob job;
    private final Duration retention;
    private final int batchSize;
    private final Clock clock;

    public ProcessedEventRetention(
            String module,
            ProcessedEventRepository repository,
            PlatformTransactionManager transactionManager,
            SingletonJobExecutor singletonJobExecutor,
            Duration retention,
            int batchSize
    ) {
        this(module, repository, transactionManager, singletonJobExecutor, retention, batchSize, Clock.systemUTC());
    }

    public ProcessedEventRetention(
            String module,
            ProcessedEventRepository repository,
            PlatformTransactionManager transactionManager,
            SingletonJobExecutor singletonJobExecutor,
            Duration retention,
            int batchSize,
            Clock clock
    ) {
        this.module = Objects.requireNonNull(module, "module");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.singletonJobExecutor = Objects.requireNonNull(singletonJobExecutor, "singletonJobExecutor");
        this.job = new SingletonJob(module + ".inbox.cleanup", Duration.ofMinutes(30), Duration.ofMinutes(10));
        this.retention = retention == null ? DEFAULT_RETENTION : retention;
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @ClusterSingletonJob("<module>.inbox.cleanup")
    @Scheduled(
            initialDelayString = "${inbox.cleanup.initial-delay-ms:600000}",
            fixedDelayString = "${inbox.cleanup.fixed-delay-ms:3600000}")
    public void purgeExpiredOnSchedule() {
        purgeExpired();
    }

    public void purgeExpired() {
        singletonJobExecutor.execute(job, this::purgeInBatches);
    }

    private void purgeInBatches() {
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
