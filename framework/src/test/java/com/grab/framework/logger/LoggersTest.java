package com.grab.framework.logger;

import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.framework.logger.internal.fixtures.TestExternalLoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoggersTest {

    @AfterEach
    void tearDown() {
        Loggers.reset();
    }

    @Test
    void getFactory_withoutConfiguredLoader_shouldUseNoOpFallback() {
        Loggers.reset();

        assertInstanceOf(com.grab.framework.logger.noop.NoOpLoggerFactory.class, Loggers.getFactory());
        assertFalse(Loggers.getLogger(LoggersTest.class).isInfoEnabled());
    }

    @Test
    void getFactory_withConfiguredLoader_shouldResolveFactory() {
        Loggers.setConfigLoader(() -> new LoggerConfig("test-external", LogLevel.INFO, Map.of(), false));

        assertInstanceOf(TestExternalLoggerFactory.class, Loggers.getFactory());
    }
}
