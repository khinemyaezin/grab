package com.grab.framework.logger.slf4j;

import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.spi.LoggerConfig;
import com.grab.framework.logger.spi.LoggerProvider;

public final class Slf4jLoggerProvider implements LoggerProvider {

    private static final String PROVIDER_ID = "slf4j";

    @Override
    public String id() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public LoggerFactory createFactory(LoggerConfig config) {
        return new Slf4jLoggerFactory();
    }
}
