package com.grab.service.sellerbff.internal.config;

import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.spi.LoggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public final class SellerLoggerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String backend = environment.getProperty("logger.backend", "slf4j");
        boolean required = environment.getProperty("logger.fail-on-missing-backend", Boolean.class, false);
        Loggers.setConfigLoader(() -> new LoggerConfig(backend, null, Map.of(), required));
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 1;
    }
}
