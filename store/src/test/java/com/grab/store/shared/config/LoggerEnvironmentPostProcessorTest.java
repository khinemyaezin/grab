package com.grab.store.shared.config;

import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.slf4j.Slf4jLoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerEnvironmentPostProcessorTest {

    private static final String LOGGER_BACKEND = "logger.backend";
    private static final String LOGGER_STRICT = "logger.fail-on-missing-backend";

    private final LoggerEnvironmentPostProcessor postProcessor =
            new LoggerEnvironmentPostProcessor();

    @AfterEach
    void tearDown() {
        System.clearProperty(LOGGER_BACKEND);
        System.clearProperty(LOGGER_STRICT);
        Loggers.reset();
    }

    @Test
    void order_shouldRunAfterConfigDataProcessor() {
        assertTrue(postProcessor.getOrder() > ConfigDataEnvironmentPostProcessor.ORDER);
    }

    @Test
    void postProcessEnvironment_shouldRespectSystemPropertyPrecedence() {
        System.setProperty(LOGGER_BACKEND, "slf4j");
        ConfigurableEnvironment environment = environmentWith(Map.of(LOGGER_BACKEND, "console"), false);

        postProcessor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertInstanceOf(Slf4jLoggerFactory.class, Loggers.getFactory());
    }

    private ConfigurableEnvironment environmentWith(Map<String, Object> values, boolean first) {
        StandardEnvironment environment = new StandardEnvironment();
        if (first) {
            environment.getPropertySources().addFirst(new MapPropertySource("test", values));
        } else {
            environment.getPropertySources().addLast(new MapPropertySource("test", values));
        }
        return environment;
    }
}
