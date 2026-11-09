package com.grab.app.shared.tracing;

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
    private static final String LOGGER_STRICT = "logger.fail-on-missing-backend";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        SpringEnvironmentLoggerConfigLoader configLoader = new SpringEnvironmentLoggerConfigLoader(environment);
        Loggers.setConfigLoader(configLoader);
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
            String rawBackend = environment.getProperty(LOGGER_BACKEND);
            String backend = trimToNull(rawBackend);
            String rawStrict = environment.getProperty(LOGGER_STRICT);
            String strict = trimToNull(rawStrict);
            boolean isStrict = parseBoolean(strict);
            Map<String, String> properties = Map.of();

            return new LoggerConfig(
                    backend,
                    null,
                    properties,
                    isStrict
            );
        }

        private boolean parseBoolean(String raw) {
            return Boolean.parseBoolean(raw);
        }

        private String trimToNull(String raw) {
            if (raw == null) {
                return null;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed;
        }
    }
}

