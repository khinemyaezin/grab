package com.grab.service.workflow.internal.config;

import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.spi.LoggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public class WorkflowLoggerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Loggers.setConfigLoader(() -> new LoggerConfig(
                environment.getProperty("logger.backend"),
                null,
                Map.of(),
                environment.getProperty("logger.fail-on-missing-backend", Boolean.class, false)));
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
