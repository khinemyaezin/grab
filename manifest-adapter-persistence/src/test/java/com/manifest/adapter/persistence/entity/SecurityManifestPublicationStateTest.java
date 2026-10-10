package com.manifest.adapter.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityManifestPublicationStateTest {

    private static class TestPublicationState extends SecurityManifestPublicationState {
        TestPublicationState(String moduleKey) {
            super(moduleKey);
        }
    }

    @Test
    void initialState_hasZeroRevisionAndEmptyDigest() {
        TestPublicationState state = new TestPublicationState("saleschannel");

        assertEquals("saleschannel", state.moduleKey());
        assertNull(state.version());
        assertEquals(0, state.revision());
        assertEquals("", state.digest());
        assertNull(state.lastEnqueuedAt());
        assertNull(state.nextPublicationAt());
    }

    @Test
    void recordEnqueued_validInitialAndSubsequentIncreases_succeeds() {
        TestPublicationState state = new TestPublicationState("saleschannel");
        Instant now = Instant.parse("2026-10-09T10:00:00Z");
        Instant next = Instant.parse("2026-10-09T10:05:00Z");

        state.recordEnqueued(1, "digest-rev-1", now, next);
        assertEquals(1, state.revision());
        assertEquals("digest-rev-1", state.digest());
        assertEquals(now, state.lastEnqueuedAt());
        assertEquals(next, state.nextPublicationAt());

        Instant laterNow = Instant.parse("2026-10-09T10:05:00Z");
        Instant laterNext = Instant.parse("2026-10-09T10:10:00Z");
        state.recordEnqueued(2, "digest-rev-2", laterNow, laterNext);
        assertEquals(2, state.revision());
        assertEquals("digest-rev-2", state.digest());
    }

    @Test
    void recordEnqueued_sameRevisionSameDigest_succeeds() {
        TestPublicationState state = new TestPublicationState("saleschannel");
        Instant now = Instant.parse("2026-10-09T10:00:00Z");
        Instant next = Instant.parse("2026-10-09T10:05:00Z");

        state.recordEnqueued(1, "digest-rev-1", now, next);
        state.recordEnqueued(1, "digest-rev-1", now.plusSeconds(300), next.plusSeconds(300));

        assertEquals(1, state.revision());
        assertEquals("digest-rev-1", state.digest());
    }

    @Test
    void recordEnqueued_regressedRevision_throwsIllegalStateException() {
        TestPublicationState state = new TestPublicationState("saleschannel");
        Instant now = Instant.parse("2026-10-09T10:00:00Z");
        Instant next = Instant.parse("2026-10-09T10:05:00Z");

        state.recordEnqueued(3, "digest-rev-3", now, next);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                state.recordEnqueued(2, "digest-rev-2", now, next));
        assertTrue(ex.getMessage().contains("cannot regress from 3 to 2"));
    }

    @Test
    void recordEnqueued_conflictingDigestOnSameRevision_throwsIllegalStateException() {
        TestPublicationState state = new TestPublicationState("saleschannel");
        Instant now = Instant.parse("2026-10-09T10:00:00Z");
        Instant next = Instant.parse("2026-10-09T10:05:00Z");

        state.recordEnqueued(1, "digest-original", now, next);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                state.recordEnqueued(1, "digest-tampered", now, next));
        assertTrue(ex.getMessage().contains("digest conflict for revision 1"));
    }
}
