package com.grab.store.shared.tracing;

import com.grab.framework.logger.slf4j.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(classes = ScheduledTraceContextAspectTest.Config.class)
class ScheduledTraceContextAspectTest {

    @Autowired
    private ScheduledProbe probe;

    @AfterEach
    void clearMdc() {
        TraceContext.clear();
    }

    @Test
    void scheduledMethod_hasGeneratedTraceIdDuringCallAndClearsAfterward() {
        probe.tick();

        assertNotNull(probe.capturedTraceId());
        UUID.fromString(probe.capturedTraceId());
        assertNull(TraceContext.current());
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {
        @Bean
        ScheduledTraceContextAspect scheduledTraceContextAspect() {
            return new ScheduledTraceContextAspect();
        }

        @Bean
        ScheduledProbe scheduledProbe() {
            return new ScheduledProbe();
        }
    }

    static class ScheduledProbe {
        private final AtomicReference<String> captured = new AtomicReference<>();

        @Scheduled(cron = "0 0 0 1 1 ?")
        public void tick() {
            captured.set(TraceContext.current());
        }

        public String capturedTraceId() {
            return captured.get();
        }
    }
}
