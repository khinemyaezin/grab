package com.grab.store.product.internal.cqrs.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CQRS infrastructure.
 * Ensures all handlers and buses are properly scanned and registered.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.grab.store.product.internal.cqrs",
        "com.grab.store.product.internal.command.handler",
        "com.grab.store.product.internal.query.handler"
})
public class CqrsConfiguration {
    // Spring will auto-discover and wire:
    // - SimpleCommandBus
    // - SimpleQueryBus
    // - All CommandHandler implementations
    // - All QueryHandler implementations
}
