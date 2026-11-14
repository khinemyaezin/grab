package com.catalog.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.claim.ConditionalUpdateClaimStrategy;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class ConditionalUpdateClaimPostgresTest extends OutboxClaimStrategyContract {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        PostgresDatabase.register(registry);
    }

    @Override
    protected OutboxClaimStrategy strategy() {
        return new ConditionalUpdateClaimStrategy();
    }
}
