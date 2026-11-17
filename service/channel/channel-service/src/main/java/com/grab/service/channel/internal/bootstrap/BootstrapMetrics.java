package com.grab.service.channel.internal.bootstrap;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * {@code bootstrap.task.status} (1 = satisfied, 0 otherwise) and {@code bootstrap.task.duration} per task.
 * Alert on {@code bootstrap_task_status{required="true"} == 0} some minutes after a rollout.
 */
@Component
public class BootstrapMetrics implements MeterBinder {

    private final BootstrapRunner runner;

    public BootstrapMetrics(BootstrapRunner runner) {
        this.runner = runner;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (BootstrapTaskStatus initial : runner.statuses()) {
            String key = initial.key();
            String required = String.valueOf(initial.required());
            Gauge.builder("bootstrap.task.status", runner, r -> satisfied(r, key))
                    .tag("task", key)
                    .tag("required", required)
                    .register(registry);
            TimeGauge.builder("bootstrap.task.duration", runner, TimeUnit.MILLISECONDS, r -> durationMillis(r, key))
                    .tag("task", key)
                    .register(registry);
        }
    }

    private static double satisfied(BootstrapRunner runner, String key) {
        BootstrapTaskStatus status = find(runner, key);
        return status != null && status.state() == BootstrapTaskStatus.State.SATISFIED ? 1.0 : 0.0;
    }

    private static double durationMillis(BootstrapRunner runner, String key) {
        BootstrapTaskStatus status = find(runner, key);
        Duration duration = status == null ? null : status.duration();
        return duration == null ? 0.0 : duration.toMillis();
    }

    private static BootstrapTaskStatus find(BootstrapRunner runner, String key) {
        return runner.statuses().stream().filter(s -> s.key().equals(key)).findFirst().orElse(null);
    }
}
