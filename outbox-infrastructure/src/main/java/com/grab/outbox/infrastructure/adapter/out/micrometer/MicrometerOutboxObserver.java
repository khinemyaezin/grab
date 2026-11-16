package com.grab.outbox.infrastructure.adapter.out.micrometer;

import com.grab.outbox.infrastructure.core.port.out.OutboxObserver;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;

/**
 * Outbox delivery metrics. Alert on {@code outbox.dead > 0} and on sustained {@code outbox.claim.lost}.
 */
public class MicrometerOutboxObserver implements OutboxObserver {

    private final MeterRegistry meterRegistry;

    public MicrometerOutboxObserver(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void published(String module, String eventType, Duration deliveryLag) {
        meterRegistry.counter("outbox.published", "module", module, "event_type", simpleName(eventType)).increment();
        meterRegistry.timer("outbox.delivery.lag", "module", module).record(deliveryLag);
    }

    @Override
    public void failed(String module, String eventType, int attempt) {
        meterRegistry.counter("outbox.failed", "module", module, "event_type", simpleName(eventType)).increment();
    }

    @Override
    public void dead(String module, String eventType) {
        meterRegistry.counter("outbox.dead", "module", module, "event_type", simpleName(eventType)).increment();
    }

    @Override
    public void claimLost(String module) {
        meterRegistry.counter("outbox.claim.lost", "module", module).increment();
    }

    @Override
    public void claimsReleased(String module, int count) {
        meterRegistry.counter("outbox.claim.released", "module", module).increment(count);
    }

    @Override
    public void cleanedUp(String module, int deleted) {
        meterRegistry.counter("outbox.cleanup.deleted", "module", module).increment(deleted);
    }

    private static String simpleName(String eventType) {
        if (eventType == null) {
            return "unknown";
        }
        int index = eventType.lastIndexOf('.');
        return index < 0 ? eventType : eventType.substring(index + 1);
    }
}
