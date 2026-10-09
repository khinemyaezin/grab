package com.manifest.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestEnvelope;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.grab.framework.security.policy.PublicationEligibilityPolicy;
import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class SecurityManifestPublicationAdapter implements SecurityManifestPublicationPort {
    private final Function<String, ? extends SecurityManifestPublicationState> lock;
    private final DomainEventProducer outbox;
    private final Function<SecurityManifestEnvelope, Event> eventFactory;
    private final Clock clock;
    private final Duration interval;

    public SecurityManifestPublicationAdapter(
            Function<String, ? extends SecurityManifestPublicationState> lock,
            DomainEventProducer outbox, Function<SecurityManifestEnvelope, Event> eventFactory,
            Clock clock, Duration interval) {
        this.lock = Objects.requireNonNull(lock);
        this.outbox = Objects.requireNonNull(outbox);
        this.eventFactory = Objects.requireNonNull(eventFactory);
        this.clock = Objects.requireNonNull(clock);
        this.interval = Objects.requireNonNull(interval);
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Publication interval must be positive");
        }
    }

    @Override
    public PublicationResult enqueue(SecurityManifest manifest) {
        SecurityManifestPublicationState state = lock.apply(manifest.moduleKey());
        if (state == null) {
            throw new IllegalStateException("Publication state is missing for module: " + manifest.moduleKey());
        }
        Instant now = clock.instant();
        PublicationResult result = PublicationEligibilityPolicy.decide(
                manifest, state.revision(), state.digest(), state.nextPublicationAt(), now);
        if (result != PublicationResult.ENQUEUED) {
            return result;
        }
        String digest = manifest.contentDigest();
        String eventId = UUID.randomUUID().toString();
        SecurityManifestEnvelope envelope = new SecurityManifestEnvelope(SecurityManifestEnvelope.TYPE, SecurityManifestEnvelope.VERSION,
                manifest.moduleKey(), eventId, now, digest, manifest);
        Event event = eventFactory.apply(envelope);
        Instant nextPublicationAt = now.plus(interval);
        state.recordEnqueued(manifest.securityRevision(), digest, now, nextPublicationAt);
        List<Event> events = List.of(event);
        outbox.produce("SecurityManifest", manifest.moduleKey(), events);
        return result;
    }
}
