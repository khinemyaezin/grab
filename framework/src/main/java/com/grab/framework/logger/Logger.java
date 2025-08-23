package com.grab.framework.logger;

public interface Logger {
    boolean isTraceEnabled();
    boolean isDebugEnabled();
    boolean isInfoEnabled();
    boolean isWarnEnabled();
    boolean isErrorEnabled();

    void trace(String message);
    void trace(String message, Object... args);
    void trace(String message, Throwable throwable);

    void debug(String message);
    void debug(String message, Object... args);
    void debug(String message, Throwable throwable);

    void info(String message);
    void info(String message, Object... args);
    void info(String message, Throwable throwable);

    void warn(String message);
    void warn(String message, Object... args);
    void warn(String message, Throwable throwable);

    void error(String message);
    void error(String message, Object... args);
    void error(String message, Throwable throwable);
}
