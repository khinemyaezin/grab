package com.grab.framework.logger.internal.fixtures;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.LoggerFactory;
import com.grab.framework.logger.noop.NoOpLogger;

public final class TestExternalLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return NoOpLogger.getInstance();
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return NoOpLogger.getInstance();
    }
}
