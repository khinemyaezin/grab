package com.grab.framework.logger.internal;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.slf4j.Slf4jLoggerFactory;
import com.grab.framework.logger.spi.LoggerConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoggerFactoryResolverTest {

    @Test
    void resolveFactory_whenSlf4jRequested_shouldReturnSlf4jFactory() {
        LoggerFactory resolved = LoggerFactoryResolver.withConfigLoader(
                () -> new LoggerConfig("slf4j", LogLevel.INFO, Map.of(), false)
        ).resolveFactory();

        assertInstanceOf(Slf4jLoggerFactory.class, resolved);
    }

    @Test
    void resolveFactory_whenMissingBackendAndStrict_shouldThrow() {
        assertThrows(
                IllegalStateException.class,
                () -> LoggerFactoryResolver.withConfigLoader(
                        () -> new LoggerConfig("does-not-exist", LogLevel.INFO, Map.of(), true)
                ).resolveFactory()
        );
    }
}
