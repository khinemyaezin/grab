package com.grab.app.shared.bootstrap;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code bootstrap} health contributor, included in the readiness group. DOWN while a required bootstrap task is
 * pending, running or failed, but only when {@code bootstrap.gate-readiness=true}; otherwise it reports UP and only
 * exposes task details, which is the rollout mode.
 */
@Component("bootstrap")
public class BootstrapHealthIndicator implements HealthIndicator {

    private final BootstrapRunner runner;

    public BootstrapHealthIndicator(BootstrapRunner runner) {
        this.runner = runner;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        for (BootstrapTaskStatus status : runner.statuses()) {
            details.put(status.key(), status);
        }
        boolean blocked = runner.isReadinessBlocked();
        Health.Builder builder = blocked && runner.gatesReadiness() ? Health.down() : Health.up();
        return builder
                .withDetail("finished", runner.isFinished())
                .withDetail("readinessBlocked", blocked)
                .withDetail("gateReadiness", runner.gatesReadiness())
                .withDetail("tasks", details)
                .build();
    }
}
