package com.grab.framework.logger;

import com.grab.framework.logger.internal.LoggerConfigLoader;
import com.grab.framework.logger.internal.LoggerFactoryResolver;

import java.util.Objects;

public final class Loggers {

    private static volatile LoggerFactory loggerFactory;
    private static volatile LoggerConfigLoader configLoader;

    private Loggers() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return getFactory().getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return getFactory().getLogger(name);
    }

    public static LoggerFactory getFactory() {
        LoggerFactory current = loggerFactory;
        if (current != null) {
            return current;
        }
        synchronized (Loggers.class) {
            if (loggerFactory == null) {
                if (configLoader == null) {
                    throw new IllegalStateException(
                            "LoggerConfigLoader is not configured. " +
                            "Configure via Loggers.setConfigLoader(...) during application bootstrap."
                    );
                }
                loggerFactory = LoggerFactoryResolver.withConfigLoader(configLoader).resolveFactory();
            }
            return loggerFactory;
        }
    }

    /**
     * Primarily for integration testing or explicit application bootstrap wiring.
     */
    public static void setFactory(LoggerFactory factory) {
        loggerFactory = Objects.requireNonNull(factory, "factory must not be null");
    }

    /**
     * Application-level override for logger config loading strategy.
     */
    public static synchronized void setConfigLoader(LoggerConfigLoader loader) {
        configLoader = Objects.requireNonNull(loader, "loader must not be null");
        loggerFactory = null;
    }

    /**
     * Primarily for test isolation.
     */
    public static synchronized void reset() {
        loggerFactory = null;
        configLoader = null;
    }
}
