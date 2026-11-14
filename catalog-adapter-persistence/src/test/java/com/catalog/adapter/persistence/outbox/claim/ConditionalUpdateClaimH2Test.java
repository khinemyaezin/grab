package com.catalog.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.claim.ConditionalUpdateClaimStrategy;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * H2 cannot skip locked rows, so only the portable strategy is configured (and tested) for it.
 */
class ConditionalUpdateClaimH2Test extends OutboxClaimStrategyContract {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:outbox-claim;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Override
    protected OutboxClaimStrategy strategy() {
        return new ConditionalUpdateClaimStrategy();
    }
}
