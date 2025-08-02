package com.grab.framework.logger;

public interface LoggerFactory {
    Logger getLogger(String name);
    Logger getLogger(Class<?> clazz);
}
