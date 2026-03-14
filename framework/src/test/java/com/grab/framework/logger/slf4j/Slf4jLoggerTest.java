package com.grab.framework.logger.slf4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Slf4jLoggerTest {

    @Test
    void info_shouldDelegateToSlf4j() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Slf4jLogger logger = new Slf4jLogger(delegate);

        logger.info("Order saved");

        verify(delegate).info("Order saved");
    }

    @Test
    void error_withThrowable_shouldDelegateToSlf4j() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        Slf4jLogger logger = new Slf4jLogger(delegate);
        RuntimeException error = new RuntimeException("boom");

        logger.error("Unhandled", error);

        verify(delegate).error("Unhandled", error);
    }

    @Test
    void isDebugEnabled_shouldMirrorDelegate() {
        org.slf4j.Logger delegate = mock(org.slf4j.Logger.class);
        when(delegate.isDebugEnabled()).thenReturn(true);
        Slf4jLogger logger = new Slf4jLogger(delegate);

        assertTrue(logger.isDebugEnabled());
        verify(delegate).isDebugEnabled();
    }
}
