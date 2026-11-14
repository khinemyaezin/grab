package com.catalog.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.adapter.out.jpa.claim.OutboxClaimStrategy;
import com.grab.outbox.infrastructure.adapter.out.jpa.claim.SkipLockedClaimStrategy;

class SkipLockedClaimPostgresTest extends PostgresOutboxContract {

    @Override
    protected OutboxClaimStrategy strategy() {
        return new SkipLockedClaimStrategy();
    }
}
