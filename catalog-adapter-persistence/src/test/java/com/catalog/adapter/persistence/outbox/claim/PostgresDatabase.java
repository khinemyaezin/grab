package com.catalog.adapter.persistence.outbox.claim;

import com.catalog.adapter.persistence.repository.config.PostgreSqlTestContainer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.DynamicPropertyRegistry;

import javax.sql.DataSource;

/**
 * Exposes the shared PostgreSQL Testcontainer to the claim contract tests.
 */
final class PostgresDatabase extends PostgreSqlTestContainer {

    private PostgresDatabase() {
    }

    static DataSource dataSource() {
        return new DriverManagerDataSource(
                POSTGRESQL.getJdbcUrl(), POSTGRESQL.getUsername(), POSTGRESQL.getPassword());
    }

    static void register(DynamicPropertyRegistry registry) {
        registerPostgresProperties(registry);
    }
}
