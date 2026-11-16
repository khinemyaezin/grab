package com.grab.scheduling.infrastructure;

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
import org.springframework.core.env.PropertyResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import javax.sql.DataSource;

/**
 * Cluster-wide singleton execution backed by a ShedLock table in the owning module's schema.
 *
 * <p>The lock provider is chosen per module by configuration ({@link LockProviderType}). The default JDBC table
 * provider uses database time (not advisory locks), so it works behind PgBouncer transaction pooling, is immune to
 * clock skew between pods, and runs on every database ShedLock supports (PostgreSQL, MySQL, MariaDB, Oracle,
 * SQL Server, DB2, H2, HSQLDB). ShedLock issues its own SQL through
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
     * JDBC provider with database time on the module's {@code <schema>.shedlock} table (the default setup).
     *
     * @param schema module schema that owns the {@code shedlock} table (created by that module's Flyway);
     *               blank means the connection's default search path
     */
    public static ShedLockSingletonJobExecutor forSchema(DataSource dataSource, String schema, MeterRegistry meterRegistry) {
        LockProviderSettings settings = new LockProviderSettings(LockProviderType.JDBC, defaultTable(schema));
        return from(settings, dataSource, meterRegistry);
    }

    /**
     * Reads the module's lock provider configuration, e.g. {@code fromProperties(ds, "catalog", env, "catalog.scheduling", registry)}:
     * <pre>
     * catalog:
     *   scheduling:
     *     lock-provider: jdbc            # jdbc (default) | jdbc-app-time | local
     *     lock-table: catalog.shedlock   # optional; defaults to &lt;schema&gt;.shedlock
     * </pre>
     */
    public static SingletonJobExecutor fromProperties(
            DataSource dataSource,
            String schema,
            PropertyResolver properties,
            String prefix,
            MeterRegistry meterRegistry
    ) {
        String typeKey = prefix + ".lock-provider";
        String typeValue = properties.getProperty(typeKey, LockProviderType.JDBC.configValue());
        LockProviderType type;
        try {
            type = LockProviderType.parse(typeValue);
        } catch (IllegalArgumentException invalid) {
            throw new IllegalArgumentException(typeKey + ": " + invalid.getMessage(), invalid);
        }
        if (type == LockProviderType.LOCAL) {
            return new LocalSingletonJobExecutor();
        }
        String table = properties.getProperty(prefix + ".lock-table", defaultTable(schema));
        LockProviderSettings settings = new LockProviderSettings(type, table);
        return from(settings, dataSource, meterRegistry);
    }

    public static ShedLockSingletonJobExecutor from(
            LockProviderSettings settings,
            DataSource dataSource,
            MeterRegistry meterRegistry
    ) {
        LockProvider lockProvider = switch (settings.type()) {
            case JDBC -> jdbcLockProvider(dataSource, settings.table(), true);
            case JDBC_APP_TIME -> jdbcLockProvider(dataSource, settings.table(), false);
            case LOCAL -> throw new IllegalArgumentException("Use LocalSingletonJobExecutor for local locks");
        };
        log.info("scheduler.locking lockProvider={} lockTable={}", settings.type().configValue(), settings.table());
        return new ShedLockSingletonJobExecutor(lockProvider, meterRegistry);
    }

    private static LockProvider jdbcLockProvider(DataSource dataSource, String table, boolean useDbTime) {
        JdbcTemplateLockProvider.Configuration.Builder builder = JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .withTableName(table);
        if (useDbTime) {
            builder.usingDbTime();
        }
        JdbcTemplateLockProvider.Configuration configuration = builder.build();
        return new JdbcTemplateLockProvider(configuration);
    }

    private static String defaultTable(String schema) {
        return schema == null || schema.isBlank() ? "shedlock" : schema + ".shedlock";
    }

    @Override
    public boolean execute(SingletonJob job, Runnable task) {
        LockConfiguration lockConfiguration = new LockConfiguration(
                Instant.now(),
                job.name(),
                job.lockAtMostFor(),
                job.lockAtLeastFor()
        );
        Timer.Sample sample = meterRegistry == null ? null : Timer.start(meterRegistry);
        LockingTaskExecutor.TaskResult<Boolean> result;
        try {
            result = lockingTaskExecutor.executeWithLock(() -> {
                task.run();
                return Boolean.TRUE;
            }, lockConfiguration);
        } catch (RuntimeException | Error exception) {
            recordDuration(sample, job.name(), "error");
            throw exception;
        } catch (Throwable throwable) {
            recordDuration(sample, job.name(), "error");
            throw new IllegalStateException("Singleton job " + job.name() + " failed", throwable);
        }

        if (!result.wasExecuted()) {
            if (meterRegistry != null) {
                meterRegistry.counter("scheduler.job.skipped_locked", "job", job.name()).increment();
            }
            log.debug("Skipped singleton job {}; lock held by another replica", job.name());
            return false;
        }
        recordDuration(sample, job.name(), "success");
        return true;
    }

    private void recordDuration(Timer.Sample sample, String job, String outcome) {
        if (sample != null) {
            sample.stop(meterRegistry.timer("scheduler.job.duration", "job", job, "outcome", outcome));
        }
    }
}
