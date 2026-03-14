package com.grab.framework.logger.console;

import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.framework.logger.spi.LoggerProvider;

public final class ConsoleLoggerProvider implements LoggerProvider {

    private static final String PROVIDER_ID = "console";

    @Override
    public String id() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 100;
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
