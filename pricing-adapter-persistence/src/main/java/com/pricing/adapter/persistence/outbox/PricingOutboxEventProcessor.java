package com.pricing.adapter.persistence.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.framework.outbox.OutboxRelay;
import com.grab.framework.scheduling.ClusterSingletonJob;
import com.grab.framework.scheduling.CompetingWorkerJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxObserver;
import com.grab.outbox.infrastructure.OutboxProcessorSettings;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

public class PricingOutboxEventProcessor extends AbstractOutboxProcessor<PricingOutboxEvent, Long> {

    public static final String MODULE = "pricing";

    public PricingOutboxEventProcessor(
            OutboxStore<PricingOutboxEvent, Long> outboxStore,
            OutboxEventSerializer serializer,
            OutboxEventDispatcher dispatcher,
            PlatformTransactionManager transactionManager,
            OutboxProcessorSettings settings,
            OutboxRelay<Long> relay,
            OutboxObserver observer,
            SingletonJobExecutor singletonJobExecutor
    ) {
        super(MODULE, outboxStore, serializer, dispatcher, transactionManager, settings, relay, observer, singletonJobExecutor);
    }

    /**
     * Competing worker (scheduling class A): runs on every replica; rows are partitioned by SKIP LOCKED claims.
     */
    @CompetingWorkerJob("claims rows with FOR UPDATE SKIP LOCKED")
    @Scheduled(fixedDelayString = "${pricing.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    /**
     * Singleton job (scheduling class B): guarded by the {@code pricing.outbox.cleanup} cluster lock.
     */
    @ClusterSingletonJob(MODULE + ".outbox.cleanup")
    @Scheduled(fixedDelayString = "${pricing.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
