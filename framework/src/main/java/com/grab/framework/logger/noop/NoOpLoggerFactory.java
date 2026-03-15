package com.grab.framework.logger.noop;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.LoggerFactory;

public final class NoOpLoggerFactory implements LoggerFactory {

    private static final NoOpLoggerFactory INSTANCE = new NoOpLoggerFactory();

    private NoOpLoggerFactory() {
    }

    public static NoOpLoggerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public Logger getLogger(String name) {
        return NoOpLogger.getInstance();
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return NoOpLogger.getInstance();
    }
}
