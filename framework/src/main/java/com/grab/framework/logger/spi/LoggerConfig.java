package com.grab.framework.logger.spi;

import com.grab.framework.logger.LogLevel;

import java.util.Map;
import java.util.Objects;

/**
 * Runtime logger backend configuration.
 */
public record LoggerConfig(
        String backend,
        LogLevel level,
        Map<String, String> options,
        boolean failOnMissingBackend
) {

    public LoggerConfig {
        options = options == null ? Map.of() : Map.copyOf(options);
    }

    public String requiredBackend() {
        return backend == null ? null : backend.trim();
    }

    public LogLevel effectiveLevel() {
        return Objects.requireNonNullElse(level, LogLevel.INFO);
    }
}

