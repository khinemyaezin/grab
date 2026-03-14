package com.grab.store.shared.config;

import com.grab.framework.logger.LogLevel;
import com.grab.framework.logger.Loggers;
import com.grab.framework.logger.spi.LoggerConfig;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;

public class LoggerTestBootstrapExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        installTestLoader();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        installTestLoader();
    }

    private void installTestLoader() {
        Loggers.setConfigLoader(() -> new LoggerConfig(
                "console",
                LogLevel.INFO,
                Map.of(),
                false
        ));
    }
}
