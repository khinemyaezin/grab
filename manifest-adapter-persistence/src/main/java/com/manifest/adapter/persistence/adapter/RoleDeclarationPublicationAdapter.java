package com.manifest.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationPublicationEnvelope;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import com.grab.framework.security.role.policy.RoleDeclarationPublicationPolicy;
import com.manifest.adapter.persistence.entity.RoleDeclarationPublicationState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class RoleDeclarationPublicationAdapter implements RoleDeclarationPublicationPort {
    private final Function<String, ? extends RoleDeclarationPublicationState> stateProvider;
    private final DomainEventProducer outbox;
    private final Function<RoleDeclarationPublicationEnvelope, Event> eventFactory;
    private final Clock clock;
    private final Duration interval;

    public RoleDeclarationPublicationAdapter(
            Function<String, ? extends RoleDeclarationPublicationState> stateProvider,
            DomainEventProducer outbox,
            Function<RoleDeclarationPublicationEnvelope, Event> eventFactory,
            Clock clock,
            Duration interval
    ) {
        this.stateProvider = Objects.requireNonNull(stateProvider);
        this.outbox = Objects.requireNonNull(outbox);
        this.eventFactory = Objects.requireNonNull(eventFactory);
        this.clock = Objects.requireNonNull(clock);
        this.interval = Objects.requireNonNull(interval);
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Publication interval must be positive");
        }
    }

    @Override
    public PublicationResult enqueue(RoleDeclaration declaration) {
        String publicationKey = declaration.owner() + ":" + declaration.roleCode();
        RoleDeclarationPublicationState state = stateProvider.apply(publicationKey);
        if (state == null) {
            throw new IllegalStateException("Role declaration publication state is missing");
        }
        Instant now = clock.instant();
        PublicationResult result = RoleDeclarationPublicationPolicy.decide(
                declaration, state.revision(), state.digest(), state.nextPublicationAt(), now);
        if (result != PublicationResult.ENQUEUED) {
            return result;
        }
        String eventId = UUID.randomUUID().toString();
        String digest = declaration.contentDigest();
        RoleDeclarationPublicationEnvelope envelope =
                new RoleDeclarationPublicationEnvelope(declaration, eventId, digest, now);
        Event event = eventFactory.apply(envelope);
        Instant nextPublicationAt = now.plus(interval);
        state.recordEnqueued(declaration.declarationRevision(), digest, now, nextPublicationAt);
        List<Event> events = List.of(event);
        outbox.produce("RoleDeclaration", publicationKey, events);
        return result;
    }
}
