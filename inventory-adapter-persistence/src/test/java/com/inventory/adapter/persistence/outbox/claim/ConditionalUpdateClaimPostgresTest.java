package com.inventory.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.adapter.out.jpa.claim.ConditionalUpdateClaimStrategy;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategy;

class ConditionalUpdateClaimPostgresTest extends PostgresOutboxContract {

    @Override
    protected OutboxClaimStrategy strategy() {
        return new ConditionalUpdateClaimStrategy();
    }
}
