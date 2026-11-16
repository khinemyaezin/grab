package com.grab.outbox.infrastructure.adapter.out.jpa.claim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxClaimStrategyCheckTest {

    @Test
    void skipLockedOnDialectWithoutSupport_failsInFailMode() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                OutboxClaimStrategyCheck.check("catalogOutboxStore", "skip-locked", "H2Dialect", false,
                        OutboxClaimStrategyCheck.Mode.FAIL));

        assertTrue(error.getMessage().contains("catalogOutboxStore"));
        assertTrue(error.getMessage().contains("H2Dialect"));
        assertTrue(error.getMessage().contains("conditional-update"));
    }

    @Test
    void skipLockedOnDialectWithoutSupport_onlyWarnsInWarnOrOffMode() {
        assertDoesNotThrow(() -> OutboxClaimStrategyCheck.check(
                "catalogOutboxStore", "skip-locked", "H2Dialect", false, OutboxClaimStrategyCheck.Mode.WARN));
        assertDoesNotThrow(() -> OutboxClaimStrategyCheck.check(
                "catalogOutboxStore", "skip-locked", "H2Dialect", false, OutboxClaimStrategyCheck.Mode.OFF));
    }

    @Test
    void supportedOrPortableCombinationsPass() {
        assertDoesNotThrow(() -> OutboxClaimStrategyCheck.check(
                "catalogOutboxStore", "skip-locked", "PostgreSQLDialect", true, OutboxClaimStrategyCheck.Mode.FAIL));
        assertDoesNotThrow(() -> OutboxClaimStrategyCheck.check(
                "catalogOutboxStore", "conditional-update", "H2Dialect", false, OutboxClaimStrategyCheck.Mode.FAIL));
    }

    @Test
    void modeParsing() {
        assertEquals(OutboxClaimStrategyCheck.Mode.WARN, OutboxClaimStrategyCheck.Mode.parse(" warn "));
        assertThrows(IllegalArgumentException.class, () -> OutboxClaimStrategyCheck.Mode.parse("strict"));
    }
}
