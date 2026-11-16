package com.grab.outbox.infrastructure.adapter.out.jpa.claim;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxStore;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Startup check for a module’s configured outbox claim strategy. It never chooses a strategy; it only logs the
 * configured one per store and rejects the single silent misconfiguration: {@code skip-locked} on a Hibernate
 * dialect that cannot skip locked rows (H2, MySQL 5.7, …). There Hibernate drops the hint, claims stay correct, but
 * competing workers serialise behind each other.
 *
 * <p>{@code outbox.claim-strategy-check}: {@code fail} (default) | {@code warn} | {@code off}.</p>
 */
public final class OutboxClaimStrategyCheck {

    private static final Logger log = Loggers.getLogger(OutboxClaimStrategyCheck.class);

    public enum Mode {
        FAIL, WARN, OFF;

        public static Mode parse(String value) {
            String normalized = Objects.requireNonNull(value, "value").trim().toUpperCase(Locale.ROOT);
            return Arrays.stream(values())
                    .filter(mode -> mode.name().equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "outbox.claim-strategy-check: unknown value '" + value + "'; expected one of "
                                    + Arrays.stream(values())
                                    .map(mode -> mode.name().toLowerCase(Locale.ROOT))
                                    .collect(Collectors.joining(", "))));
        }
    }

    private OutboxClaimStrategyCheck() {
    }

    public static void verify(String beanName, JpaOutboxStore<?, ?> store, Mode mode) {
        OutboxClaimStrategy strategy = store.claimStrategy();
        Dialect dialect = dialectOf(store.entityManagerFactory());
        String dialectName = dialect == null ? "unknown" : dialect.getClass().getSimpleName();
        boolean supportsSkipLocked = dialect != null && dialect.supportsSkipLocked();
        log.info("outbox.locking store={} claimStrategy={} dialect={} supportsSkipLocked={}",
                beanName, strategy.name(), dialectName, supportsSkipLocked);
        check(beanName, strategy.name(), dialectName, dialect == null || supportsSkipLocked, mode);
    }

    /**
     * @param skipLockedUsable false only when the dialect is known and lacks skip-locked support
     */
    static void check(String beanName, String strategyName, String dialectName, boolean skipLockedUsable, Mode mode) {
        if (mode == Mode.OFF || skipLockedUsable || !SkipLockedClaimStrategy.NAME.equals(strategyName)) {
            return;
        }
        String message = beanName + " uses claim-strategy=skip-locked but " + dialectName
                + " cannot skip locked rows; workers would serialise. Set <module>.outbox.claim-strategy="
                + "conditional-update (or outbox.claim-strategy-check=warn to accept it)";
        if (mode == Mode.FAIL) {
            throw new IllegalStateException(message);
        }
        log.warn(message);
    }

    private static Dialect dialectOf(EntityManagerFactory entityManagerFactory) {
        try {
            SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
            return sessionFactory.getJdbcServices().getDialect();
        } catch (RuntimeException notHibernate) {
            log.debug("Cannot resolve Hibernate dialect for outbox claim check", notHibernate);
            return null;
        }
    }
}
