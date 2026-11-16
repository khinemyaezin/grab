package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import org.springframework.core.env.PropertyResolver;

import java.time.Duration;
import java.util.List;

public final class OutboxScheduledJobs {

    private OutboxScheduledJobs() {
    }

    public static List<ScheduledJob> of(String module, Runnable poll, SingletonJob cleanupJob, Runnable cleanup,
                                        SingletonJobExecutor executor, PropertyResolver properties) {
        String prefix = module + ".outbox.";
        long pollDelay = properties.getProperty(prefix + "fixed-delay-ms", Long.class, 5000L);
        long cleanupDelay = properties.getProperty(prefix + "cleanup-fixed-delay-ms", Long.class, 300000L);
        return List.of(
                new CompetingWorkerTask(prefix + "poll", "outbox row claims",
                        Duration.ZERO, Duration.ofMillis(pollDelay), poll),
                new ClusterSingletonTask(cleanupJob, executor, Duration.ZERO, Duration.ofMillis(cleanupDelay), cleanup));
    }
}
