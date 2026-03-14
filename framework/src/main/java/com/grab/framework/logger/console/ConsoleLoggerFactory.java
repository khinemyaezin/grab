package com.grab.framework.logger.console;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.LoggerFactory;

import java.util.Objects;

public class ConsoleLoggerFactory implements LoggerFactory {
    private final LogLevel level;

    public ConsoleLoggerFactory(LogLevel level) {
        this.level = Objects.requireNonNullElse(level, LogLevel.INFO);
    }

    @Override
    public Logger getLogger(String name) {
        return new ConsoleLogger(name, level);
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new ConsoleLogger(clazz.getName(), level);
    }
}
