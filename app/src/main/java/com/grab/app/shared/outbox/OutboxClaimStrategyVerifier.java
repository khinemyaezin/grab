package com.grab.app.shared.outbox;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.OutboxStore;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.claim.SkipLockedClaimStrategy;
import com.grab.outbox.infrastructure.jpa.JpaOutboxStore;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Startup check for the manually configured outbox claim strategies. It never chooses a strategy; it only logs the
 * configured one per store and rejects the single silent misconfiguration: {@code skip-locked} on a Hibernate
 * dialect that cannot skip locked rows (H2, MySQL 5.7, …). There Hibernate drops the hint, claims stay correct, but
 * competing workers serialise behind each other.
 *
 * <p>{@code outbox.claim-strategy-check}: {@code fail} (default) | {@code warn} | {@code off}.</p>
 */
public final class OutboxClaimStrategyVerifier implements SmartInitializingSingleton {

    private static final Logger log = Loggers.getLogger(OutboxClaimStrategyVerifier.class);

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

    private final ListableBeanFactory beanFactory;
    private final Mode mode;

    public OutboxClaimStrategyVerifier(ListableBeanFactory beanFactory, Mode mode) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory");
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterSingletonsInstantiated() {
        Map<String, OutboxStore> stores = beanFactory.getBeansOfType(OutboxStore.class);
        stores.forEach((beanName, store) -> {
            if (store instanceof JpaOutboxStore<?, ?> jpaStore) {
                verifyStore(beanName, jpaStore);
            }
        });
    }

    private void verifyStore(String beanName, JpaOutboxStore<?, ?> store) {
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
