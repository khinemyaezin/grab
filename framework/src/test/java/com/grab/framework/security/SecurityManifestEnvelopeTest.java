package com.grab.framework.security;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityManifestEnvelopeTest {
    @Test
    void envelope_roundTripsWithoutJavaClassTransportIdentifiers() throws Exception {
        var manifest = new SecurityManifest("merchant", 2, List.of(new ScopeDeclaration("merchant.account", null)), List.of());
        var envelope = new SecurityManifestEnvelope(SecurityManifestEnvelope.TYPE, 1, "merchant", "event-1", Instant.now(), manifest.contentDigest(), manifest);
        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        String json = mapper.writeValueAsString(envelope);
        assertFalse(json.contains("com.grab"));
        assertEquals(envelope, mapper.readValue(json, SecurityManifestEnvelope.class));
    }

    @Test
    void envelope_foreignProducerOrUnsupportedVersion_isRejected() {
        var manifest = new SecurityManifest("merchant", 2, List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> new SecurityManifestEnvelope(SecurityManifestEnvelope.TYPE, 1, "inventory", "event", Instant.now(), manifest.contentDigest(), manifest));
        assertThrows(IllegalArgumentException.class, () -> new SecurityManifestEnvelope(SecurityManifestEnvelope.TYPE, 2, "merchant", "event", Instant.now(), manifest.contentDigest(), manifest));
    }
}
