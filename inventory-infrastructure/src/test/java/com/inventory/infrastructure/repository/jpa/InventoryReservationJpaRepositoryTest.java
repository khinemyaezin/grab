package com.inventory.infrastructure.repository.jpa;

import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.repository.jpa.config.RepositoryTestConfig;
import com.inventory.infrastructure.view.InventoryReservationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryReservationJpaRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private InventoryReservationJpaRepository inventoryReservationJpaRepository;

    private InventoryReservationEntity activeReservation1;
    private InventoryReservationEntity activeReservation2;
    private InventoryReservationEntity fulfilledReservation;
    private InventoryReservationEntity cancelledReservation;

    @BeforeEach
    void setUp() {
        inventoryReservationJpaRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        activeReservation1 = new InventoryReservationEntity();
        activeReservation1.setUuid("uuid-res-1");
        activeReservation1.setInventoryItemUuid("uuid-inv-1");
        activeReservation1.setOrderId("order-1");
        activeReservation1.setOrderLineId("line-1");
        activeReservation1.setQuantity(5);
        activeReservation1.setStatus(InventoryReservationStatus.ACTIVE);
        activeReservation1.setExpiresAt(now.plusHours(24));
        activeReservation1.setIdempotencyKey("idemp-key-1");
        activeReservation1.setCreatedAt(now);
        activeReservation1.setUpdatedAt(now);

        activeReservation2 = new InventoryReservationEntity();
        activeReservation2.setUuid("uuid-res-2");
        activeReservation2.setInventoryItemUuid("uuid-inv-1");
        activeReservation2.setOrderId("order-2");
        activeReservation2.setOrderLineId("line-2");
        activeReservation2.setQuantity(3);
        activeReservation2.setStatus(InventoryReservationStatus.ACTIVE);
        activeReservation2.setExpiresAt(now.plusHours(12));
        activeReservation2.setIdempotencyKey("idemp-key-2");
        activeReservation2.setCreatedAt(now);
        activeReservation2.setUpdatedAt(now);

        fulfilledReservation = new InventoryReservationEntity();
        fulfilledReservation.setUuid("uuid-res-3");
        fulfilledReservation.setInventoryItemUuid("uuid-inv-1");
        fulfilledReservation.setOrderId("order-1");
        fulfilledReservation.setOrderLineId("line-3");
        fulfilledReservation.setQuantity(2);
        fulfilledReservation.setStatus(InventoryReservationStatus.FULFILLED);
        fulfilledReservation.setIdempotencyKey("idemp-key-3");
        fulfilledReservation.setCreatedAt(now);
        fulfilledReservation.setUpdatedAt(now);

        cancelledReservation = new InventoryReservationEntity();
        cancelledReservation.setUuid("uuid-res-4");
        cancelledReservation.setInventoryItemUuid("uuid-inv-2");
        cancelledReservation.setOrderId("order-3");
        cancelledReservation.setOrderLineId("line-4");
        cancelledReservation.setQuantity(1);
        cancelledReservation.setStatus(InventoryReservationStatus.CANCELLED);
        cancelledReservation.setIdempotencyKey("idemp-key-4");
        cancelledReservation.setCreatedAt(now);
        cancelledReservation.setUpdatedAt(now);

        inventoryReservationJpaRepository.saveAll(List.of(activeReservation1, activeReservation2, fulfilledReservation, cancelledReservation));
    }

    @Test
    void findByUuid_returnsEntity_whenExists() {
        Optional<InventoryReservationEntity> result = inventoryReservationJpaRepository.findByUuid("uuid-res-1");

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo("order-1");
        assertThat(result.get().getQuantity()).isEqualTo(5);
    }

    @Test
    void findByUuid_returnsEmpty_whenNotExists() {
        Optional<InventoryReservationEntity> result = inventoryReservationJpaRepository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdempotencyKey_returnsEntity_whenExists() {
        Optional<InventoryReservationEntity> result = inventoryReservationJpaRepository.findByIdempotencyKey("idemp-key-1");

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo("uuid-res-1");
        assertThat(result.get().getOrderId()).isEqualTo("order-1");
    }

    @Test
    void findByIdempotencyKey_returnsEmpty_whenNotExists() {
        Optional<InventoryReservationEntity> result = inventoryReservationJpaRepository.findByIdempotencyKey("non-existent-key");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByInventoryItemUuid_returnsAllReservationsForItem() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByInventoryItemUuid("uuid-inv-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(InventoryReservationView::uuid)
                .containsExactlyInAnyOrder("uuid-res-1", "uuid-res-2", "uuid-res-3");
    }

    @Test
    void findAllByInventoryItemUuid_returnsEmptyForUnknownItem() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByInventoryItemUuid("unknown-uuid", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByInventoryItemUuid_respectsPagination() {
        Page<InventoryReservationView> page0 = inventoryReservationJpaRepository.findAllByInventoryItemUuid("uuid-inv-1", PageRequest.of(0, 2));
        Page<InventoryReservationView> page1 = inventoryReservationJpaRepository.findAllByInventoryItemUuid("uuid-inv-1", PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(3);
        assertThat(page0.getTotalPages()).isEqualTo(2);
        assertThat(page1.getContent()).hasSize(1);
    }

    @Test
    void findAllByOrderIdAndStatus_returnsMatchingReservations() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByOrderIdAndStatus("order-1", InventoryReservationStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).uuid()).isEqualTo("uuid-res-1");
        assertThat(result.getContent().get(0).status()).isEqualTo(InventoryReservationStatus.ACTIVE);
    }

    @Test
    void findAllByOrderIdAndStatus_returnsFulfilledReservations() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByOrderIdAndStatus("order-1", InventoryReservationStatus.FULFILLED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).uuid()).isEqualTo("uuid-res-3");
    }

    @Test
    void findAllByOrderIdAndStatus_returnsEmptyWhenNoMatch() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByOrderIdAndStatus("order-1", InventoryReservationStatus.CANCELLED, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByOrderIdAndStatus_returnsEmptyForUnknownOrder() {
        Page<InventoryReservationView> result = inventoryReservationJpaRepository.findAllByOrderIdAndStatus("unknown-order", InventoryReservationStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }
}
