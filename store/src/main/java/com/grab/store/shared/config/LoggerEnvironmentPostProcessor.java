package com.grab.store.shared.config;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.internal.LoggerConfigLoader;
import com.grab.framework.logger.spi.LoggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public class LoggerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String LOGGER_BACKEND = "logger.backend";
    private static final String LOGGER_LEVEL = "logger.level";
    private static final String LOGGER_STRICT = "logger.fail-on-missing-backend";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Loggers.setConfigLoader(new SpringEnvironmentLoggerConfigLoader(environment));
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }

    private static class SpringEnvironmentLoggerConfigLoader implements LoggerConfigLoader {

        private final ConfigurableEnvironment environment;

        private SpringEnvironmentLoggerConfigLoader(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public LoggerConfig load() {
            String backend = trimToNull(environment.getProperty(LOGGER_BACKEND));
            String level = trimToNull(environment.getProperty(LOGGER_LEVEL));
            String strict = trimToNull(environment.getProperty(LOGGER_STRICT));

            return new LoggerConfig(
                    backend,
                    parseLevel(level),
                    Map.of(),
                    parseBoolean(strict)
            );
        }

        private LogLevel parseLevel(String raw) {
            if (raw == null) {
                return LogLevel.INFO;
            }
            try {
                return LogLevel.valueOf(raw.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return LogLevel.INFO;
            }
        }

        private boolean parseBoolean(String raw) {
            return Boolean.parseBoolean(raw);
        }

        private String trimToNull(String raw) {
            if (raw == null) {
                return null;
            }
            String trimmed = raw.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
