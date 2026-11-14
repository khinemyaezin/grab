package com.grab.scheduling.infrastructure;

import java.time.Duration;

public sealed interface ScheduledJob permits CompetingWorkerTask, ClusterSingletonTask {

    String name();

    Duration initialDelay();

    Duration fixedDelay();
}
