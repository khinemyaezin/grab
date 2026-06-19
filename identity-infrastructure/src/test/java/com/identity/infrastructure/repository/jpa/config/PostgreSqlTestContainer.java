package com.identity.infrastructure.repository.jpa.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgreSqlTestContainer {

    protected static final PostgreSQLContainer<?> POSTGRESQL;

    static {
        POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("identity")
                .withUsername("identity")
                .withPassword("identity");
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    protected static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.show_sql", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
