package com.grab.discovery.adapter.persistence.adapter;

import com.grab.discovery.adapter.persistence.exception.DiscoveryInfraException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscoveryPersistenceExecutorTest {

    private final DiscoveryPersistenceExecutor executor = new DiscoveryPersistenceExecutor();

    @Test
    void command_expectedVersionConflict_throwsDiscoveryInfraException() {
        ObjectOptimisticLockingFailureException conflict = new ObjectOptimisticLockingFailureException(
                "DiscoveryProductEntity",
                "product-1"
        );

        assertThatThrownBy(() -> executor.command("DiscoveryProductEntity", () -> {
            throw conflict;
        }))
                .isInstanceOf(DiscoveryInfraException.class)
                .hasCause(conflict);
    }

    @Test
    void command_projectionInsertionRace_throwsDiscoveryInfraException() {
        DataIntegrityViolationException conflict = new DataIntegrityViolationException(
                "duplicate key violates constraint uq_discovery_product_channel_product"
        );

        assertThatThrownBy(() -> executor.command("DiscoveryProductEntity", () -> {
            throw conflict;
        }))
                .isInstanceOf(DiscoveryInfraException.class)
                .hasCause(conflict);
    }
}
