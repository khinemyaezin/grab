package com.grab.framework.logger.console;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Logger;

public class ConsoleLogger implements Logger {
    private final String name;
    private final LogLevel level;

    public ConsoleLogger(String name, LogLevel level) {
        this.name = name;
        this.level = level;
    }

    private boolean isEnabled(LogLevel logLevel) {
        return logLevel.ordinal() >= this.level.ordinal();
    }

    private void log(LogLevel level, String message, Object... args) {
        if (isEnabled(level)) {
            System.out.printf("[%s] [%s] %s%n", level.name(), name, String.format(message, args));
        }
    }

    private void log(LogLevel level, String message, Throwable t) {
        if (isEnabled(level)) {
            System.out.printf("[%s] [%s] %s - %s%n", level.name(), name, message, t);
        }
    }

    public boolean isTraceEnabled() { return isEnabled(LogLevel.TRACE); }
    public boolean isDebugEnabled() { return isEnabled(LogLevel.DEBUG); }
    public boolean isInfoEnabled()  { return isEnabled(LogLevel.INFO); }
    public boolean isWarnEnabled()  { return isEnabled(LogLevel.WARN); }
    public boolean isErrorEnabled() { return isEnabled(LogLevel.ERROR); }

    public void trace(String message)                      { log(LogLevel.TRACE, message); }
    public void trace(String message, Object... args)     { log(LogLevel.TRACE, message, args); }
    public void trace(String message, Throwable throwable){ log(LogLevel.TRACE, message, throwable); }

    public void debug(String message)                      { log(LogLevel.DEBUG, message); }
    public void debug(String message, Object... args)     { log(LogLevel.DEBUG, message, args); }
    public void debug(String message, Throwable throwable){ log(LogLevel.DEBUG, message, throwable); }

    public void info(String message)                       { log(LogLevel.INFO, message); }
    public void info(String message, Object... args)      { log(LogLevel.INFO, message, args); }
    public void info(String message, Throwable throwable) { log(LogLevel.INFO, message, throwable); }

    public void warn(String message)                       { log(LogLevel.WARN, message); }
    public void warn(String message, Object... args)      { log(LogLevel.WARN, message, args); }
    public void warn(String message, Throwable throwable) { log(LogLevel.WARN, message, throwable); }

    public void error(String message)                       { log(LogLevel.ERROR, message); }
    public void error(String message, Object... args)      { log(LogLevel.ERROR, message, args); }
    public void error(String message, Throwable throwable) { log(LogLevel.ERROR, message, throwable); }
}

