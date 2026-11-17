package com.grab.service.account.internal.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param gateReadiness  when {@code false} (rollout default) the bootstrap health indicator reports UP regardless of
 *                       task state, so tasks are observed without holding traffic
 * @param maxAttempts    retries per task for transient failures
 * @param initialBackoff first retry delay, doubled per attempt up to {@code maxBackoff}
 * @param maxBackoff     retry delay cap
 * @param lockPollInterval how often a replica that lost the lock re-tries it
 */
@ConfigurationProperties(prefix = "bootstrap")
public record BootstrapProperties(Boolean gateReadiness, Integer maxAttempts, Duration initialBackoff,
                                  Duration maxBackoff, Duration lockPollInterval) {

    public BootstrapProperties {
        gateReadiness = gateReadiness != null && gateReadiness;
        maxAttempts = maxAttempts == null ? 5 : Math.max(1, maxAttempts);
        initialBackoff = initialBackoff == null ? Duration.ofSeconds(2) : initialBackoff;
        maxBackoff = maxBackoff == null ? Duration.ofSeconds(30) : maxBackoff;
        lockPollInterval = lockPollInterval == null ? Duration.ofSeconds(2) : lockPollInterval;
    }
}
