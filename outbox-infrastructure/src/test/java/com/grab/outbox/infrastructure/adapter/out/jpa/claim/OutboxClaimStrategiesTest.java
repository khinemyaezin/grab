package com.grab.outbox.infrastructure.adapter.out.jpa.claim;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxClaimStrategiesTest {

    @Test
    void defaultsToSkipLockedWhenPropertyIsAbsent() {
        PropertyResolver environment = properties(Map.of());

        OutboxClaimStrategy strategy = OutboxClaimStrategies.fromProperties(environment, "catalog.outbox");

        assertInstanceOf(SkipLockedClaimStrategy.class, strategy);
        assertEquals("skip-locked", strategy.name());
    }

    @Test
    void readsModuleScopedProperty() {
        PropertyResolver environment = properties(Map.of(
                "catalog.outbox.claim-strategy", "conditional-update",
                "pricing.outbox.claim-strategy", "skip-locked"));

        assertInstanceOf(ConditionalUpdateClaimStrategy.class,
                OutboxClaimStrategies.fromProperties(environment, "catalog.outbox"));
        assertInstanceOf(SkipLockedClaimStrategy.class,
                OutboxClaimStrategies.fromProperties(environment, "pricing.outbox"));
    }

    @Test
    void parseAcceptsEnumNamesAndIgnoresCaseAndWhitespace() {
        assertEquals(OutboxClaimStrategies.Type.CONDITIONAL_UPDATE, OutboxClaimStrategies.Type.parse(" CONDITIONAL_UPDATE "));
        assertEquals(OutboxClaimStrategies.Type.SKIP_LOCKED, OutboxClaimStrategies.Type.parse("Skip-Locked"));
    }

    @Test
    void unknownValueFailsWithPropertyNameAndAllowedValues() {
        PropertyResolver environment = properties(Map.of("catalog.outbox.claim-strategy", "skiplocked"));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> OutboxClaimStrategies.fromProperties(environment, "catalog.outbox"));

        assertTrue(error.getMessage().contains("catalog.outbox.claim-strategy"));
        assertTrue(error.getMessage().contains("skip-locked, conditional-update"));
    }

    private static PropertyResolver properties(Map<String, Object> values) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        return environment;
    }
}
