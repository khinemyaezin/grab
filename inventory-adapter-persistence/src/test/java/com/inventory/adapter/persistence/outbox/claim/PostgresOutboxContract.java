package com.inventory.adapter.persistence.outbox.claim;

import com.grab.outbox.infrastructure.adapter.out.jpa.OutboxRowFactory;
import com.grab.outbox.infrastructure.contract.OutboxStoreContract;
import com.inventory.adapter.persistence.outbox.InventoryOutboxEvent;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ContextConfiguration(classes = PostgresOutboxContract.Config.class)
abstract class PostgresOutboxContract extends OutboxStoreContract<InventoryOutboxEvent> {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        PostgresDatabase.register(registry);
    }

    @Override
    protected Class<InventoryOutboxEvent> entityType() {
        return InventoryOutboxEvent.class;
    }

    @Override
    protected OutboxRowFactory<InventoryOutboxEvent> rowFactory() {
        return InventoryOutboxEvent::pending;
    }

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = InventoryOutboxEvent.class)
    static class Config {
    }
}
