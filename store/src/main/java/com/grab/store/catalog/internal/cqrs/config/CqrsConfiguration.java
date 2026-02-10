package com.grab.store.catalog.internal.cqrs.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CQRS infrastructure.
 * Ensures all handlers and buses are properly scanned and registered.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.grab.store.catalog.internal.cqrs",
        "com.grab.store.catalog.internal.command.handler",
        "com.grab.store.catalog.internal.query.handler"
})
public class CqrsConfiguration {
    // Spring will auto-discover and wire:
    // - SimpleCommandBus
    // - SimpleQueryBus
    // - All CommandHandler implementations
    // - All QueryHandler implementations
}
