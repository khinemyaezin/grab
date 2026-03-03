package com.inventory.infrastructure.mapper.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.enums.StockMovementType;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.mapper.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryJpaAssemblerImplTest {

    private InventoryItemEntityMapper inventoryItemEntityMapper;
    private StockMovementEntityMapper stockMovementEntityMapper;
    private StockMovementMapper stockMovementMapper;
    private InventoryItemMapper inventoryItemMapper;
    private InventoryJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        inventoryItemEntityMapper = mock(InventoryItemEntityMapper.class);
        stockMovementEntityMapper = mock(StockMovementEntityMapper.class);
        stockMovementMapper = mock(StockMovementMapper.class);
        inventoryItemMapper = mock(InventoryItemMapper.class);
        assembler = new InventoryJpaAssemblerImpl(
                inventoryItemEntityMapper,
                stockMovementEntityMapper,
                stockMovementMapper,
                inventoryItemMapper
        );
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_shouldCreateNewEntity() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, null);

        assertNotNull(result);
        verify(inventoryItemEntityMapper).toEntity(eq(inventoryItem), any(InventoryItemEntity.class));
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_shouldCreateAllMovements() {
        InventoryItem inventoryItem = createInventoryItemWithMovements();

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, null);

        assertEquals(inventoryItem.getMovements().size(), result.getMovements().size());
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_shouldMapMovementPropertiesCorrectly() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        StockMovement movement = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        inventoryItem = addMovementToInventoryItem(inventoryItem, movement);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, null);

        assertEquals(1, result.getMovements().size());
        StockMovementEntity movementEntity = result.getMovements().getFirst();
        assertEquals("mov-1", movementEntity.getUuid());
        assertSame(result, movementEntity.getInventoryItem());
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_shouldSetBackReferenceToInventoryItem() {
        InventoryItem inventoryItem = createInventoryItemWithMovements();

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, null);

        for (StockMovementEntity movementEntity : result.getMovements()) {
            assertSame(result, movementEntity.getInventoryItem());
        }
    }

    @Test
    void buildFullEntityGraph_withExistingEntity_shouldMergeIntoExistingEntity() {
        InventoryItem inventoryItem = createInventoryItemWithMovements();
        InventoryItemEntity existingEntity = createExistingEntity(inventoryItem);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        assertSame(existingEntity, result);
        verify(inventoryItemEntityMapper).toEntity(eq(inventoryItem), same(existingEntity));
    }

    @Test
    void buildFullEntityGraph_withExistingMovement_shouldKeepExistingMovementEntity() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        StockMovement movement = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        inventoryItem = addMovementToInventoryItem(inventoryItem, movement);

        InventoryItemEntity existingEntity = new InventoryItemEntity();
        existingEntity.setUuid("inv-1");
        StockMovementEntity existingMovement = new StockMovementEntity();
        existingMovement.setUuid("mov-1");
        existingMovement.setType(StockMovementType.PURCHASE_ORDER_RECEIPT);
        existingMovement.setQuantity(100);
        existingEntity.addMovement(existingMovement);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        assertEquals(1, result.getMovements().size());
        assertSame(existingMovement, result.getMovements().getFirst());
    }

    @Test
    void buildFullEntityGraph_withNewMovementOnExistingEntity_shouldAddNewMovement() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        StockMovement existingMovement = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement newMovement = createStockMovement("mov-new", StockMovementType.SALE, 10, 100, "ORD-001");
        inventoryItem = addMovementToInventoryItem(inventoryItem, existingMovement);
        inventoryItem = addMovementToInventoryItem(inventoryItem, newMovement);

        InventoryItemEntity existingEntity = new InventoryItemEntity();
        existingEntity.setUuid("inv-1");
        StockMovementEntity existingMovementEntity = new StockMovementEntity();
        existingMovementEntity.setUuid("mov-1");
        existingEntity.addMovement(existingMovementEntity);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        assertEquals(2, result.getMovements().size());
        List<String> uuids = result.getMovements().stream()
                .map(StockMovementEntity::getUuid)
                .toList();
        assertTrue(uuids.contains("mov-1"));
        assertTrue(uuids.contains("mov-new"));
    }

    @Test
    void buildFullEntityGraph_withOrphanMovement_shouldRemoveMovementNotInDomain() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        // inventoryItem has no movements

        InventoryItemEntity existingEntity = new InventoryItemEntity();
        existingEntity.setUuid("inv-1");
        StockMovementEntity orphanMovement = new StockMovementEntity();
        orphanMovement.setUuid("orphan-mov");
        orphanMovement.setType(StockMovementType.PURCHASE_ORDER_RECEIPT);
        existingEntity.addMovement(orphanMovement);

        assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        assertTrue(existingEntity.getMovements().isEmpty());
    }

    @Test
    void buildFullEntityGraph_withMixedMovementChanges_shouldHandleAddUpdateRemove() {
        // Domain has: mov-1 (keep), mov-3 (new)
        // Existing has: mov-1 (keep), mov-2 (remove)
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        StockMovement mov1 = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement mov3 = createStockMovement("mov-3", StockMovementType.SALE, 10, 100, "ORD-003");
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov1);
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov3);

        InventoryItemEntity existingEntity = new InventoryItemEntity();
        existingEntity.setUuid("inv-1");

        StockMovementEntity existingMov1 = new StockMovementEntity();
        existingMov1.setUuid("mov-1");
        existingEntity.addMovement(existingMov1);

        StockMovementEntity existingMov2 = new StockMovementEntity();
        existingMov2.setUuid("mov-2");
        existingEntity.addMovement(existingMov2);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        List<String> uuids = result.getMovements().stream()
                .map(StockMovementEntity::getUuid)
                .toList();
        assertEquals(2, uuids.size());
        assertTrue(uuids.contains("mov-1"));
        assertTrue(uuids.contains("mov-3"));
        assertFalse(uuids.contains("mov-2"));
    }

    @Test
    void buildFullEntityGraph_withComplexMovementScenario_shouldHandleMultipleOperations() {
        // Domain has: mov-1 (existing), mov-3 (new), mov-4 (new)
        // Existing has: mov-1 (keep), mov-2 (remove), mov-5 (remove)
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        StockMovement mov1 = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement mov3 = createStockMovement("mov-3", StockMovementType.SALE, 10, 100, "ORD-003");
        StockMovement mov4 = createStockMovement("mov-4", StockMovementType.CYCLE_COUNT_ADJUSTMENT, 5, 90, "ADJ-001");
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov1);
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov3);
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov4);

        InventoryItemEntity existingEntity = new InventoryItemEntity();
        existingEntity.setUuid("inv-1");

        StockMovementEntity existingMov1 = new StockMovementEntity();
        existingMov1.setUuid("mov-1");
        existingEntity.addMovement(existingMov1);

        StockMovementEntity existingMov2 = new StockMovementEntity();
        existingMov2.setUuid("mov-2");
        existingEntity.addMovement(existingMov2);

        StockMovementEntity existingMov5 = new StockMovementEntity();
        existingMov5.setUuid("mov-5");
        existingEntity.addMovement(existingMov5);

        stubMovementEntityMapper();

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        List<String> uuids = result.getMovements().stream()
                .map(StockMovementEntity::getUuid)
                .toList();
        assertEquals(3, uuids.size());
        assertTrue(uuids.contains("mov-1"));
        assertTrue(uuids.contains("mov-3"));
        assertTrue(uuids.contains("mov-4"));
        assertFalse(uuids.contains("mov-2"));
        assertFalse(uuids.contains("mov-5"));
    }

    @Test
    void toFullDomainGraph_withEntity_shouldMapEntityToDomain() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("inv-1");
        entity.setSku("SKU-001");
        entity.setProductVariantId("var-1");
        entity.setLocationId("loc-1");

        InventoryItem expectedInventoryItem = createInventoryItem("inv-1", "SKU-001");
        when(inventoryItemMapper.toDomain(eq(entity), anyList())).thenReturn(expectedInventoryItem);

        InventoryItem result = assembler.toFullDomainGraph(entity);

        assertSame(expectedInventoryItem, result);
    }

    @Test
    void toFullDomainGraph_withMovements_shouldMapAllMovementsCorrectly() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("inv-1");
        entity.setSku("SKU-001");

        StockMovementEntity movementEntity1 = new StockMovementEntity();
        movementEntity1.setUuid("mov-1");
        movementEntity1.setType(StockMovementType.PURCHASE_ORDER_RECEIPT);
        entity.addMovement(movementEntity1);

        StockMovementEntity movementEntity2 = new StockMovementEntity();
        movementEntity2.setUuid("mov-2");
        movementEntity2.setType(StockMovementType.SALE);
        entity.addMovement(movementEntity2);

        StockMovement domainMovement1 = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement domainMovement2 = createStockMovement("mov-2", StockMovementType.SALE, 10, 100, "ORD-001");

        when(stockMovementMapper.toDomain(movementEntity1)).thenReturn(domainMovement1);
        when(stockMovementMapper.toDomain(movementEntity2)).thenReturn(domainMovement2);

        InventoryItem expectedInventoryItem = createInventoryItem("inv-1", "SKU-001");
        when(inventoryItemMapper.toDomain(eq(entity), anyList())).thenReturn(expectedInventoryItem);

        assembler.toFullDomainGraph(entity);

        verify(stockMovementMapper).toDomain(movementEntity1);
        verify(stockMovementMapper).toDomain(movementEntity2);
        verify(inventoryItemMapper).toDomain(eq(entity), argThat(movements ->
                movements.size() == 2 &&
                        movements.getFirst() == domainMovement1 &&
                        movements.get(1) == domainMovement2
        ));
    }

    @Test
    void toFullDomainGraph_withNoMovements_shouldMapEntityOnly() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("inv-1");
        entity.setSku("SKU-001");
        entity.setProductVariantId("var-1");
        entity.setLocationId("loc-1");

        InventoryItem expectedInventoryItem = createInventoryItem("inv-1", "SKU-001");
        when(inventoryItemMapper.toDomain(eq(entity), eq(List.of()))).thenReturn(expectedInventoryItem);

        InventoryItem result = assembler.toFullDomainGraph(entity);

        assertSame(expectedInventoryItem, result);
        verify(inventoryItemMapper).toDomain(entity, List.of());
        verifyNoInteractions(stockMovementMapper);
    }

    @Test
    void toFullDomainGraph_withMultipleMovements_shouldPreserveMovementOrder() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("inv-1");
        entity.setSku("SKU-001");

        StockMovementEntity movementEntity1 = new StockMovementEntity();
        movementEntity1.setUuid("mov-1");
        StockMovementEntity movementEntity2 = new StockMovementEntity();
        movementEntity2.setUuid("mov-2");
        StockMovementEntity movementEntity3 = new StockMovementEntity();
        movementEntity3.setUuid("mov-3");

        entity.addMovement(movementEntity1);
        entity.addMovement(movementEntity2);
        entity.addMovement(movementEntity3);

        StockMovement domainMovement1 = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement domainMovement2 = createStockMovement("mov-2", StockMovementType.SALE, 10, 100, "ORD-001");
        StockMovement domainMovement3 = createStockMovement("mov-3", StockMovementType.CYCLE_COUNT_ADJUSTMENT, 5, 90, "ADJ-001");

        when(stockMovementMapper.toDomain(movementEntity1)).thenReturn(domainMovement1);
        when(stockMovementMapper.toDomain(movementEntity2)).thenReturn(domainMovement2);
        when(stockMovementMapper.toDomain(movementEntity3)).thenReturn(domainMovement3);

        InventoryItem expectedInventoryItem = createInventoryItem("inv-1", "SKU-001");
        when(inventoryItemMapper.toDomain(eq(entity), anyList())).thenReturn(expectedInventoryItem);

        assembler.toFullDomainGraph(entity);

        verify(inventoryItemMapper).toDomain(eq(entity), argThat(movements ->
                movements.size() == 3 &&
                        movements.getFirst() == domainMovement1 &&
                        movements.get(1) == domainMovement2 &&
                        movements.get(2) == domainMovement3
        ));
    }

    // ========== Helper Methods ==========

    private void stubMovementEntityMapper() {
        doAnswer(invocation -> {
            StockMovement source = invocation.getArgument(0);
            StockMovementEntity target = invocation.getArgument(1);
            target.setUuid(source.getId().getValue());
            target.setType(source.getType());
            target.setQuantity(source.getQuantity());
            target.setQuantityBefore(source.getQuantityBefore());
            target.setQuantityAfter(source.getQuantityAfter());
            target.setReferenceId(source.getReferenceId());
            target.setCreatedAt(source.getCreatedAt());
            target.setCreatedBy(source.getCreatedBy() != null ? source.getCreatedBy().getValue() : null);
            return null;
        }).when(stockMovementEntityMapper).toEntity(any(StockMovement.class), any(StockMovementEntity.class));
    }

    private InventoryItem createInventoryItem(String id, String sku) {
        return new InventoryItem(
                id(id),
                sku,
                id("variant-1"),
                id("location-1"),
                InventoryQuantity.withOnHand(100),
                ReorderConfig.defaultConfig(),
                InventoryStatus.ACTIVE,
                new ArrayList<>(),
                LocalDateTime.now()
        );
    }

    private InventoryItem createInventoryItemWithMovements() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");

        StockMovement mov1 = createStockMovement("mov-1", StockMovementType.PURCHASE_ORDER_RECEIPT, 100, 0, "PO-001");
        StockMovement mov2 = createStockMovement("mov-2", StockMovementType.SALE, 10, 100, "ORD-001");

        inventoryItem = addMovementToInventoryItem(inventoryItem, mov1);
        inventoryItem = addMovementToInventoryItem(inventoryItem, mov2);

        return inventoryItem;
    }

    private InventoryItem addMovementToInventoryItem(InventoryItem inventoryItem, StockMovement movement) {
        List<StockMovement> movements = new ArrayList<>(inventoryItem.getMovements());
        movements.add(movement);
        return new InventoryItem(
                inventoryItem.getId(),
                inventoryItem.getSku(),
                inventoryItem.getProductVariantId(),
                inventoryItem.getLocationId(),
                inventoryItem.getQuantity(),
                inventoryItem.getReorderConfig(),
                inventoryItem.getStatus(),
                movements,
                inventoryItem.getLastUpdated()
        );
    }

    private StockMovement createStockMovement(String id, StockMovementType type, int quantity, int quantityBefore, String referenceId) {
        int quantityAfter = calculateQuantityAfter(type, quantityBefore, quantity);
        return new StockMovement(
                id(id),
                type,
                quantity,
                quantityBefore,
                quantityAfter,
                referenceId,
                LocalDateTime.now(),
                id("user-1")
        );
    }

    private int calculateQuantityAfter(StockMovementType type, int before, int quantity) {
        return switch (type) {
            case PURCHASE_ORDER_RECEIPT, CUSTOMER_RETURN, TRANSFER_IN, INITIAL_STOCK, RESERVATION_RELEASE ->
                    before + quantity;
            case SALE, TRANSFER_OUT, RETURN_TO_VENDOR, WRITE_OFF, RESERVATION ->
                    before - quantity;
            case CYCLE_COUNT_ADJUSTMENT, DAMAGE_ADJUSTMENT, SHRINKAGE ->
                    before + quantity;
        };
    }

    private InventoryItemEntity createExistingEntity(InventoryItem inventoryItem) {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid(inventoryItem.getId().getValue());
        entity.setSku(inventoryItem.getSku());
        entity.setProductVariantId(inventoryItem.getProductVariantId().getValue());
        entity.setLocationId(inventoryItem.getLocationId().getValue());
        entity.setOnHand(inventoryItem.getQuantity().onHand());
        entity.setReserved(inventoryItem.getQuantity().reserved());
        entity.setInTransit(inventoryItem.getQuantity().inTransit());
        entity.setDamaged(inventoryItem.getQuantity().damaged());
        entity.setSafetyStock(inventoryItem.getReorderConfig().safetyStock());
        entity.setReorderPoint(inventoryItem.getReorderConfig().reorderPoint());
        entity.setReorderQuantity(inventoryItem.getReorderConfig().reorderQuantity());
        entity.setMaxStock(inventoryItem.getReorderConfig().maxStock());
        entity.setStatus(inventoryItem.getStatus());

        for (StockMovement movement : inventoryItem.getMovements()) {
            StockMovementEntity movementEntity = new StockMovementEntity();
            movementEntity.setUuid(movement.getId().getValue());
            movementEntity.setType(movement.getType());
            movementEntity.setQuantity(movement.getQuantity());
            movementEntity.setQuantityBefore(movement.getQuantityBefore());
            movementEntity.setQuantityAfter(movement.getQuantityAfter());
            movementEntity.setReferenceId(movement.getReferenceId());
            movementEntity.setCreatedAt(movement.getCreatedAt());
            movementEntity.setCreatedBy(movement.getCreatedBy() != null ? movement.getCreatedBy().getValue() : null);
            entity.addMovement(movementEntity);
        }

        return entity;
    }

    private static Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Id other)) return false;
                return Objects.equals(value, other.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }
}
