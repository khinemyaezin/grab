package com.grab.framework.logger.internal;

import com.grab.framework.logger.spi.LoggerConfig;

public interface LoggerConfigLoader {
    LoggerConfig load();
}

