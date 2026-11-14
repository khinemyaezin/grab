package com.grab.scheduling.infrastructure;

import java.time.Duration;
import java.util.Objects;

public record CompetingWorkerTask(String name, String partitioning, Duration initialDelay,
                                  Duration fixedDelay, Runnable body) implements ScheduledJob {

    public CompetingWorkerTask {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(partitioning, "partitioning");
        Objects.requireNonNull(body, "body");
    }
}
