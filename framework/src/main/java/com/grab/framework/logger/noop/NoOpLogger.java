package com.grab.framework.logger.noop;

import com.grab.framework.logger.Logger;

public final class NoOpLogger implements Logger {

    private static final NoOpLogger INSTANCE = new NoOpLogger();

    private NoOpLogger() {
    }

    public static NoOpLogger getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void trace(String message) {
    }

    @Override
    public void trace(String message, Object... args) {
    }

    @Override
    public void trace(String message, Throwable throwable) {
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(String message, Object... args) {
    }

    @Override
    public void debug(String message, Throwable throwable) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void info(String message, Object... args) {
    }

    @Override
    public void info(String message, Throwable throwable) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void warn(String message, Object... args) {
    }

    @Override
    public void warn(String message, Throwable throwable) {
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void error(String message, Object... args) {
    }

    @Override
    public void error(String message, Throwable throwable) {
    }
}
