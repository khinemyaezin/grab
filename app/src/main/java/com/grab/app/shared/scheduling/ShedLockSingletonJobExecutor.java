package com.grab.app.shared.scheduling;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.scheduling.SingletonJob;
import com.grab.framework.scheduling.SingletonJobExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Instant;

/**
 * Cluster-wide singleton execution backed by a ShedLock table in the owning module's schema.
 *
 * <p>Uses the JDBC table provider with database time (not advisory locks), so it works behind PgBouncer
 * transaction pooling and is immune to clock skew between pods. ShedLock issues its own SQL through
 * {@link JdbcTemplate}; it is infrastructure, not a Spring Data repository.</p>
 */
public final class ShedLockSingletonJobExecutor implements SingletonJobExecutor {

    private static final Logger log = Loggers.getLogger(ShedLockSingletonJobExecutor.class);

    private final LockingTaskExecutor lockingTaskExecutor;
    private final MeterRegistry meterRegistry;

    public ShedLockSingletonJobExecutor(LockProvider lockProvider, MeterRegistry meterRegistry) {
        this.lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        this.meterRegistry = meterRegistry;
    }

    /**
     * @param schema module schema that owns the {@code shedlock} table (created by that module's Flyway);
     *               blank means the connection's default search path
     */
    public static ShedLockSingletonJobExecutor forSchema(DataSource dataSource, String schema, MeterRegistry meterRegistry) {
        JdbcTemplateLockProvider lockProvider = new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .withTableName(schema == null || schema.isBlank() ? "shedlock" : schema + ".shedlock")
                        .usingDbTime()
                        .build()
        );
        return new ShedLockSingletonJobExecutor(lockProvider, meterRegistry);
    }

    @Override
    public boolean execute(SingletonJob job, Runnable task) {
        LockConfiguration lockConfiguration = new LockConfiguration(
                Instant.now(),
                job.name(),
                job.lockAtMostFor(),
                job.lockAtLeastFor()
        );
        Timer.Sample sample = Timer.start(meterRegistry);
        LockingTaskExecutor.TaskResult<Boolean> result;
        try {
            result = lockingTaskExecutor.executeWithLock(() -> {
                task.run();
                return Boolean.TRUE;
            }, lockConfiguration);
        } catch (RuntimeException | Error exception) {
            sample.stop(meterRegistry.timer("scheduler.job.duration", "job", job.name(), "outcome", "error"));
            throw exception;
        } catch (Throwable throwable) {
            sample.stop(meterRegistry.timer("scheduler.job.duration", "job", job.name(), "outcome", "error"));
            throw new IllegalStateException("Singleton job " + job.name() + " failed", throwable);
        }

        if (!result.wasExecuted()) {
            meterRegistry.counter("scheduler.job.skipped_locked", "job", job.name()).increment();
            log.debug("Skipped singleton job {}; lock held by another replica", job.name());
            return false;
        }
        sample.stop(meterRegistry.timer("scheduler.job.duration", "job", job.name(), "outcome", "success"));
        return true;
    }
}
