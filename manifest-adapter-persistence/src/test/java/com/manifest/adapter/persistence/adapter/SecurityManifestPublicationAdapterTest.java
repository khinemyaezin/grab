package com.manifest.adapter.persistence.adapter;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.security.AuthorityDefinition;
import com.grab.framework.security.SecurityManifest;
import com.grab.framework.security.SecurityManifestPublicationPort;
import com.manifest.adapter.persistence.entity.SecurityManifestPublicationState;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityManifestPublicationAdapterTest {

    private record TestEvent(String payload) implements Event {
    }

    private static class TestState extends SecurityManifestPublicationState {
        TestState(String moduleKey) {
            super(moduleKey);
        }
    }

    private static class CapturingEventProducer implements DomainEventProducer {
        final List<Event> produced = new ArrayList<>();

        @Override
        public void produce(String aggregateType, String aggregateId, List<Event> events) {
            produced.addAll(events);
        }
    }

    @Test
    void enqueue_missingState_throwsIllegalStateException() {
        CapturingEventProducer outbox = new CapturingEventProducer();
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneOffset.UTC);
        SecurityManifestPublicationAdapter adapter = new SecurityManifestPublicationAdapter(
                moduleKey -> null,
                outbox,
                envelope -> new TestEvent("test"),
                clock,
                Duration.ofMinutes(5)
        );

        SecurityManifest manifest = new SecurityManifest(
                "saleschannel",
                1,
                Collections.emptyList(),
                List.of(new AuthorityDefinition("SC_READ", "Sales Channel", "Read access"))
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> adapter.enqueue(manifest));
        assertEquals("Publication state is missing for module: saleschannel", ex.getMessage());
    }

    @Test
    void enqueue_validManifest_recordsStateAndProducesOutboxEvent() {
        TestState state = new TestState("saleschannel");
        CapturingEventProducer outbox = new CapturingEventProducer();
        Instant now = Instant.parse("2026-10-09T10:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);

        SecurityManifestPublicationAdapter adapter = new SecurityManifestPublicationAdapter(
                moduleKey -> state,
                outbox,
                envelope -> new TestEvent("test"),
                clock,
                Duration.ofMinutes(5)
        );

        SecurityManifest manifest = new SecurityManifest(
                "saleschannel",
                1,
                Collections.emptyList(),
                List.of(new AuthorityDefinition("SC_READ", "Sales Channel", "Read access"))
        );

        SecurityManifestPublicationPort.PublicationResult result = adapter.enqueue(manifest);

        assertEquals(SecurityManifestPublicationPort.PublicationResult.ENQUEUED, result);
        assertEquals(1, state.revision());
        assertEquals(manifest.contentDigest(), state.digest());
        assertEquals(now, state.lastEnqueuedAt());
        assertEquals(now.plus(Duration.ofMinutes(5)), state.nextPublicationAt());
        assertEquals(1, outbox.produced.size());
    }

    @Test
    void enqueue_lowerRevision_returnsSupersededAndDoesNotProduceEvent() {
        TestState state = new TestState("saleschannel");
        state.recordEnqueued(2, "digest-rev-2", Instant.parse("2026-10-09T09:00:00Z"), Instant.parse("2026-10-09T09:05:00Z"));

        CapturingEventProducer outbox = new CapturingEventProducer();
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneOffset.UTC);

        SecurityManifestPublicationAdapter adapter = new SecurityManifestPublicationAdapter(
                moduleKey -> state,
                outbox,
                envelope -> new TestEvent("test"),
                clock,
                Duration.ofMinutes(5)
        );

        SecurityManifest manifest = new SecurityManifest(
                "saleschannel",
                1,
                Collections.emptyList(),
                List.of(new AuthorityDefinition("SC_READ", "Sales Channel", "Read access"))
        );

        SecurityManifestPublicationPort.PublicationResult result = adapter.enqueue(manifest);

        assertEquals(SecurityManifestPublicationPort.PublicationResult.SUPERSEDED, result);
        assertEquals(2, state.revision());
        assertEquals(0, outbox.produced.size());
    }

    @Test
    void enqueue_sameRevisionDifferentDigest_returnsConflictAndDoesNotProduceEvent() {
        TestState state = new TestState("saleschannel");
        state.recordEnqueued(1, "different-digest", Instant.parse("2026-10-09T09:00:00Z"), Instant.parse("2026-10-09T09:05:00Z"));

        CapturingEventProducer outbox = new CapturingEventProducer();
        Clock clock = Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneOffset.UTC);

        SecurityManifestPublicationAdapter adapter = new SecurityManifestPublicationAdapter(
                moduleKey -> state,
                outbox,
                envelope -> new TestEvent("test"),
                clock,
                Duration.ofMinutes(5)
        );

        SecurityManifest manifest = new SecurityManifest(
                "saleschannel",
                1,
                Collections.emptyList(),
                List.of(new AuthorityDefinition("SC_READ", "Sales Channel", "Read access"))
        );

        SecurityManifestPublicationPort.PublicationResult result = adapter.enqueue(manifest);

        assertEquals(SecurityManifestPublicationPort.PublicationResult.CONFLICT, result);
        assertEquals(0, outbox.produced.size());
    }

    @Test
    void constructor_zeroOrNegativeInterval_throwsIllegalArgumentException() {
        CapturingEventProducer outbox = new CapturingEventProducer();
        Clock clock = Clock.systemUTC();

        assertThrows(IllegalArgumentException.class, () -> new SecurityManifestPublicationAdapter(
                moduleKey -> null, outbox, envelope -> new TestEvent("test"), clock, Duration.ZERO));

        assertThrows(IllegalArgumentException.class, () -> new SecurityManifestPublicationAdapter(
                moduleKey -> null, outbox, envelope -> new TestEvent("test"), clock, Duration.ofSeconds(-5)));
    }
}
