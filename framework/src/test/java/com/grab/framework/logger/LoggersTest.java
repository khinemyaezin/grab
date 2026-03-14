package com.grab.framework.logger;

import com.grab.framework.logger.spi.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoggersTest {

    @AfterEach
    void tearDown() {
        Loggers.reset();
    }

    @Test
    void getFactory_withoutConfiguredLoader_shouldThrow() {
        Loggers.reset();

        assertThrows(IllegalStateException.class, Loggers::getFactory);
    }

    @Test
    void getFactory_withConfiguredLoader_shouldResolveFactory() {
        Loggers.setConfigLoader(() -> new LoggerConfig("slf4j", LogLevel.INFO, Map.of(), false));

        assertInstanceOf(com.grab.framework.logger.slf4j.Slf4jLoggerFactory.class, Loggers.getFactory());
    }
}
