package com.catalog.adapter.persistence.outbox.claim;

import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Exposes the shared PostgreSQL Testcontainer to the claim contract tests.
 */
final class PostgresDatabase extends PostgreSqlTestContainer {

    private PostgresDatabase() {
    }

    static void register(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry);
    }
}
