package com.grab.framework.logger.internal.fixtures;

import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.console.ConsoleLoggerFactory;
import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.framework.logger.spi.LoggerProvider;

public final class TestExternalLoggerProvider implements LoggerProvider {

    @Override
    public String id() {
        return "test-external";
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public LoggerFactory createFactory(LoggerConfig config) {
        return new ConsoleLoggerFactory(config.effectiveLevel());
    }
}
