package com.inventory.infrastructure.mapper.jpa.impl;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.mapper.jpa.InventoryItemEntityMapper;
import com.inventory.infrastructure.mapper.jpa.InventoryItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryJpaAssemblerImplTest {

    private InventoryItemEntityMapper inventoryItemEntityMapper;
    private InventoryItemMapper inventoryItemMapper;
    private InventoryJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        inventoryItemEntityMapper = mock(InventoryItemEntityMapper.class);
        inventoryItemMapper = mock(InventoryItemMapper.class);
        assembler = new InventoryJpaAssemblerImpl(
                inventoryItemEntityMapper,
                inventoryItemMapper
        );
    }

    @Test
    void toEntity_withNoExistingEntity_shouldCreateNewEntity() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, null);

        assertNotNull(result);
        verify(inventoryItemEntityMapper).toEntity(eq(inventoryItem), any(InventoryItemEntity.class));
    }

    @Test
    void toEntity_withExistingEntity_shouldMergeIntoExistingEntity() {
        InventoryItem inventoryItem = createInventoryItem("inv-1", "SKU-001");
        InventoryItemEntity existingEntity = createExistingEntity(inventoryItem);

        InventoryItemEntity result = assembler.buildFullEntityGraph(inventoryItem, existingEntity);

        assertSame(existingEntity, result);
        verify(inventoryItemEntityMapper).toEntity(eq(inventoryItem), same(existingEntity));
    }

    @Test
    void toDomain_withEntity_shouldMapEntityToDomain() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("inv-1");
        entity.setSku("SKU-001");
        entity.setProductVariantId("var-1");
        entity.setLocationId("loc-1");

        InventoryItem expectedInventoryItem = createInventoryItem("inv-1", "SKU-001");
        when(inventoryItemMapper.toDomain(entity)).thenReturn(expectedInventoryItem);

        InventoryItem result = assembler.toFullDomainGraph(entity);

        assertSame(expectedInventoryItem, result);
        verify(inventoryItemMapper).toDomain(entity);
    }

    private InventoryItem createInventoryItem(String id, String sku) {
        return new InventoryItem(
                id(id),
                sku,
                id("seller-1"),
                id("variant-1"),
                id("location-1"),
                InventoryQuantity.withOnHand(100),
                ReorderConfig.defaultConfig(),
                InventoryStatus.ACTIVE,
                LocalDateTime.now()
        );
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
