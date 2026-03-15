package com.grab.framework.logger.spi;

import com.grab.framework.logger.LoggerFactory;

/**
 * Pluggable logger backend contract.
 */
public interface LoggerProvider {

    String id();

    int priority();

    boolean isAvailable();

    LoggerFactory createFactory(LoggerConfig config);
}

