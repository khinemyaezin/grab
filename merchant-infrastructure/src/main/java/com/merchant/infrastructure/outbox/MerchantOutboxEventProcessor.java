package com.merchant.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEventDispatcher;
import com.grab.framework.outbox.OutboxEventSerializer;
import com.grab.outbox.infrastructure.AbstractOutboxProcessor;
import com.grab.outbox.infrastructure.OutboxStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;

public class MerchantOutboxEventProcessor extends AbstractOutboxProcessor<MerchantOutboxEvent, Long> {
    public MerchantOutboxEventProcessor(OutboxStore<MerchantOutboxEvent, Long> store,
                                        OutboxEventSerializer serializer,
                                        OutboxEventDispatcher dispatcher,
                                        PlatformTransactionManager transactionManager,
                                        int batchSize, Duration retryDelay,
                                        Duration claimTimeout, Duration retention) {
        super(store, serializer, dispatcher, transactionManager, batchSize, retryDelay, claimTimeout, retention);
    }

    @Scheduled(fixedDelayString = "${merchant.outbox.fixed-delay-ms:5000}")
    public void processAvailableEventsOnSchedule() {
        processAvailableEvents();
    }

    @Scheduled(fixedDelayString = "${merchant.outbox.cleanup-fixed-delay-ms:300000}")
    public void cleanupPublishedEventsOnSchedule() {
        cleanupPublishedEvents();
    }
}
