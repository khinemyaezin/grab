package com.inventory.domain.support;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.console.ConsoleLoggerFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class InventoryDomainLoggerExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        Loggers.setFactory(new ConsoleLoggerFactory(LogLevel.ERROR));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Loggers.reset();
    }
}
