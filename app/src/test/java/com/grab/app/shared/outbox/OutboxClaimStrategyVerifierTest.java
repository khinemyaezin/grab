package com.grab.app.shared.outbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxClaimStrategyVerifierTest {

    @Test
    void skipLockedOnDialectWithoutSupport_failsInFailMode() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                OutboxClaimStrategyVerifier.check("catalogOutboxStore", "skip-locked", "H2Dialect", false,
                        OutboxClaimStrategyVerifier.Mode.FAIL));

        assertTrue(error.getMessage().contains("catalogOutboxStore"));
        assertTrue(error.getMessage().contains("H2Dialect"));
        assertTrue(error.getMessage().contains("conditional-update"));
    }

    @Test
    void skipLockedOnDialectWithoutSupport_onlyWarnsInWarnOrOffMode() {
        assertDoesNotThrow(() -> OutboxClaimStrategyVerifier.check(
                "catalogOutboxStore", "skip-locked", "H2Dialect", false, OutboxClaimStrategyVerifier.Mode.WARN));
        assertDoesNotThrow(() -> OutboxClaimStrategyVerifier.check(
                "catalogOutboxStore", "skip-locked", "H2Dialect", false, OutboxClaimStrategyVerifier.Mode.OFF));
    }

    @Test
    void supportedOrPortableCombinationsPass() {
        assertDoesNotThrow(() -> OutboxClaimStrategyVerifier.check(
                "catalogOutboxStore", "skip-locked", "PostgreSQLDialect", true, OutboxClaimStrategyVerifier.Mode.FAIL));
        assertDoesNotThrow(() -> OutboxClaimStrategyVerifier.check(
                "catalogOutboxStore", "conditional-update", "H2Dialect", false, OutboxClaimStrategyVerifier.Mode.FAIL));
    }

    @Test
    void modeParsing() {
        assertEquals(OutboxClaimStrategyVerifier.Mode.WARN, OutboxClaimStrategyVerifier.Mode.parse(" warn "));
        assertThrows(IllegalArgumentException.class, () -> OutboxClaimStrategyVerifier.Mode.parse("strict"));
    }
}
