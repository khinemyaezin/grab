package com.grab.framework.logger.slf4j;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.LoggerFactory;

public final class Slf4jLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(name));
    }

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new Slf4jLogger(org.slf4j.LoggerFactory.getLogger(clazz));
    }
}
