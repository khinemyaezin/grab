package com.catalog.adapter.persistence.outbox.claim;

import com.catalog.adapter.persistence.outbox.CatalogOutboxEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.OutboxRowFactory;
import com.grab.outbox.infrastructure.contract.OutboxStoreContract;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ContextConfiguration(classes = PostgresOutboxContract.Config.class)
abstract class PostgresOutboxContract extends OutboxStoreContract<CatalogOutboxEvent> {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        PostgresDatabase.register(registry);
    }

    @Override
    protected Class<CatalogOutboxEvent> entityType() {
        return CatalogOutboxEvent.class;
    }

    @Override
    protected OutboxRowFactory<CatalogOutboxEvent> rowFactory() {
        return CatalogOutboxEvent::pending;
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = CatalogOutboxEvent.class)
    static class Config {
    }
}
