package com.grab.framework.logger.internal;

import com.grab.framework.logger.spi.LoggerProvider;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerProviderRegistryTest {

    @Test
    void registry_shouldIncludeBuiltInProviders() {
        LoggerProviderRegistry registry = new LoggerProviderRegistry();

        Set<String> ids = registry.all().stream()
                .map(LoggerProvider::id)
                .collect(Collectors.toSet());

        assertTrue(ids.contains("slf4j"));
        assertTrue(ids.contains("console"));
        assertTrue(ids.contains("test-external"));
    }

    @Test
    void highestPriorityAvailable_shouldPreferSlf4jByDefault() {
        LoggerProviderRegistry registry = new LoggerProviderRegistry();

        String selected = registry.highestPriorityAvailable()
                .map(LoggerProvider::id)
                .orElse("none");

        assertEquals("slf4j", selected);
    }
}
