package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.springframework.core.env.PropertyResolver;

import java.time.Duration;

public final class InboxScheduledJobs {

    private InboxScheduledJobs() {
    }

    public static ClusterSingletonTask of(String module, Runnable purge, SingletonJobExecutor executor,
                                          PropertyResolver properties) {
        String prefix = "inbox.cleanup.";
        Duration initial = delay(module, prefix + "initial-delay-ms", 600000L, properties);
        Duration fixed = delay(module, prefix + "fixed-delay-ms", 3600000L, properties);
        SingletonJob job = new SingletonJob(module + ".inbox.cleanup", Duration.ofMinutes(30), Duration.ofMinutes(10));
        return new ClusterSingletonTask(job, executor, initial, fixed, purge);
    }

    private static Duration delay(String module, String key, long defaultMillis, PropertyResolver properties) {
        long fallback = properties.getProperty(key, Long.class, defaultMillis);
        long delay = properties.getProperty(module + "." + key, Long.class, fallback);
        return Duration.ofMillis(delay);
    }
}
