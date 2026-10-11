package com.manifest.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.role.RoleDeclaration;
import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;
import com.grab.framework.security.role.RolePermissionReference;
import com.manifest.adapter.persistence.entity.RoleDeclarationPublicationState;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleDeclarationPublicationAdapterTest {
    private record TestEvent(String eventId) implements Event {
    }

    private static final class TestState extends RoleDeclarationPublicationState {
        private TestState(String publicationKey) {
            super(publicationKey);
        }
    }

    private static final class CapturingOutbox implements DomainEventProducer {
        private final List<Event> events = new ArrayList<>();

        @Override
        public void produce(String aggregateType, String aggregateId, List<Event> producedEvents) {
            events.addAll(producedEvents);
        }
    }

    @Test
    void enqueuePublishesOncePerIntervalAndKeepsRevisionDurableInState() {
        Instant firstTime = Instant.parse("2026-10-10T00:00:00Z");
        TestState state = new TestState("merchant:MERCHANT_ADMIN");
        CapturingOutbox outbox = new CapturingOutbox();
        RoleDeclaration declaration = declaration();
        RoleDeclarationPublicationAdapter firstAdapter = adapter(state, outbox, firstTime);

        PublicationResult first = firstAdapter.enqueue(declaration);
        PublicationResult throttled = firstAdapter.enqueue(declaration);

        assertEquals(PublicationResult.ENQUEUED, first);
        assertEquals(PublicationResult.NOT_DUE, throttled);
        assertEquals(1, state.revision());
        assertEquals(declaration.contentDigest(), state.digest());
        assertEquals(firstTime.plus(Duration.ofMinutes(5)), state.nextPublicationAt());
        assertEquals(1, outbox.events.size());

        RoleDeclarationPublicationAdapter repairAdapter = adapter(state, outbox, firstTime.plus(Duration.ofMinutes(5)));
        PublicationResult repaired = repairAdapter.enqueue(declaration);

        assertEquals(PublicationResult.ENQUEUED, repaired);
        assertEquals(2, outbox.events.size());
    }

    private RoleDeclarationPublicationAdapter adapter(TestState state, CapturingOutbox outbox, Instant now) {
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        return new RoleDeclarationPublicationAdapter(
                publicationKey -> state,
                outbox,
                envelope -> new TestEvent(envelope.eventId()),
                clock,
                Duration.ofMinutes(5));
    }

    private RoleDeclaration declaration() {
        return new RoleDeclaration(
                "merchant", "MERCHANT_ADMIN", "merchant.account", 1,
                Map.of("merchant", 1), List.of(new RolePermissionReference("merchant", "MERCHANT_PROFILE_READ")));
    }
}
