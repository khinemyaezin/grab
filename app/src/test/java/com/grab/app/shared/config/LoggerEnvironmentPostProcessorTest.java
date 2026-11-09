package com.grab.app.shared.config;

import com.grab.app.shared.tracing.LoggerEnvironmentPostProcessor;
import com.grab.framework.logger.LoggerFactory;
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

    private final LoggerEnvironmentPostProcessor postProcessor = new LoggerEnvironmentPostProcessor();

    @AfterEach
    void tearDown() {
        System.clearProperty(LOGGER_BACKEND);
        System.clearProperty(LOGGER_STRICT);
        Loggers.reset();
    }

    @Test
    void order_whenQueried_shouldRunAfterConfigDataProcessor() {
        int actualOrder = postProcessor.getOrder();
        int expectedThreshold = ConfigDataEnvironmentPostProcessor.ORDER;
        boolean isAfter = actualOrder > expectedThreshold;
        assertTrue(isAfter);
    }

    @Test
    void postProcessEnvironment_whenSystemPropertyProvided_shouldRespectSystemPropertyPrecedence() {
        System.setProperty(LOGGER_BACKEND, "slf4j");
        Map<String, Object> properties = Map.of(LOGGER_BACKEND, "console");
        ConfigurableEnvironment environment = environmentWith(properties, false);
        SpringApplication application = new SpringApplication(Object.class);

        postProcessor.postProcessEnvironment(environment, application);

        LoggerFactory factory = Loggers.getFactory();
        assertInstanceOf(Slf4jLoggerFactory.class, factory);
    }

    private ConfigurableEnvironment environmentWith(Map<String, Object> values, boolean first) {
        StandardEnvironment environment = new StandardEnvironment();
        MapPropertySource propertySource = new MapPropertySource("test", values);
        if (first) {
            environment.getPropertySources().addFirst(propertySource);
        } else {
            environment.getPropertySources().addLast(propertySource);
        }
        return environment;
    }
}

