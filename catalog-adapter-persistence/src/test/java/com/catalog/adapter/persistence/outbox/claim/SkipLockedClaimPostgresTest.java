package com.catalog.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.claim.SkipLockedClaimStrategy;
import com.grab.outbox.infrastructure.claim.OutboxClaimStrategy;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class SkipLockedClaimPostgresTest extends OutboxClaimStrategyContract {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        PostgresDatabase.register(registry);
    }

    @Override
    protected OutboxClaimStrategy strategy() {
        return new SkipLockedClaimStrategy();
    }
}
