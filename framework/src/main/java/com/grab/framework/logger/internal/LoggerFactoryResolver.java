package com.grab.framework.logger.internal;

import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.console.ConsoleLoggerFactory;
import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.framework.logger.spi.LoggerProvider;

import java.util.Objects;

public final class LoggerFactoryResolver {

    private final LoggerProviderRegistry providerRegistry;
    private final LoggerConfigLoader configLoader;

    public LoggerFactoryResolver(
            LoggerProviderRegistry providerRegistry,
            LoggerConfigLoader configLoader
    ) {
        this.providerRegistry = Objects.requireNonNull(providerRegistry, "providerRegistry must not be null");
        this.configLoader = Objects.requireNonNull(configLoader, "configLoader must not be null");
    }

    public static LoggerFactoryResolver withConfigLoader(LoggerConfigLoader configLoader) {
        return new LoggerFactoryResolver(
                new LoggerProviderRegistry(),
                configLoader
        );
    }

    public LoggerFactory resolveFactory() {
        LoggerConfig config = configLoader.load();
        String requested = config.requiredBackend();
        if (requested != null) {
            LoggerProvider requestedProvider = providerRegistry.findById(requested)
                    .filter(LoggerProvider::isAvailable)
                    .orElse(null);
            if (requestedProvider != null) {
                return requestedProvider.createFactory(config);
            }
            if (config.failOnMissingBackend()) {
                throw new IllegalStateException(
                        "Configured logger backend is unavailable: " + requested
                );
            }
        }

        return providerRegistry.highestPriorityAvailable()
                .map(provider -> provider.createFactory(config))
                .orElseGet(() -> new ConsoleLoggerFactory(config.effectiveLevel()));
    }
}
