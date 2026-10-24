package com.pricing.adapter.persistence.repository.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class PostgreSqlTestContainer {
    @Container
    protected static final PostgreSQLContainer<?> POSTGRESQL;

    static {
        POSTGRESQL = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("pricing")
                .withUsername("pricing")
                .withPassword("pricing");
    }

    @DynamicPropertySource
    protected static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
