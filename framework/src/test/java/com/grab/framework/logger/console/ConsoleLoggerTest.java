package com.grab.framework.logger.console;

import com.grab.framework.logger.LogLevel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleLoggerTest {

    @Test
    void info_withBracePlaceholders_shouldRenderArguments() throws Exception {
        ConsoleLogger logger = new ConsoleLogger("test", LogLevel.INFO);

        String output = captureStdOut(() -> logger.info("Saved {} for {}", "item", "store"));

        assertTrue(output.contains("Saved item for store"));
    }

    @Test
    void error_withTrailingThrowable_shouldPrintStackTrace() throws Exception {
        ConsoleLogger logger = new ConsoleLogger("test", LogLevel.INFO);
        RuntimeException boom = new RuntimeException("boom");

        String output = captureStdOut(() -> logger.error("Failed {}", "catalog", boom));

        assertTrue(output.contains("Failed catalog"));
        assertTrue(output.contains("java.lang.RuntimeException: boom"));
    }

    private String captureStdOut(CheckedRunnable runnable) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        try {
            System.setOut(capture);
            runnable.run();
        } finally {
            System.setOut(original);
            capture.close();
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
