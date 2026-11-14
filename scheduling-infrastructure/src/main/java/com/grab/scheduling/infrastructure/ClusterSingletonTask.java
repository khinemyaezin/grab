package com.grab.scheduling.infrastructure;

import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;

import java.time.Duration;
import java.util.Objects;

public record ClusterSingletonTask(SingletonJob job, SingletonJobExecutor executor, Duration initialDelay,
                                   Duration fixedDelay, Runnable body) implements ScheduledJob {

    public ClusterSingletonTask {
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(body, "body");
    }

    @Override
    public String name() {
        return job.name();
    }
}
