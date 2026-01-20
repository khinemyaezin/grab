package com.inventory.infrastructure.entity.meta;

import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.StockMovementEntity;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.LocalDateTime;

@StaticMetamodel(InventoryItemEntity.class)
public class InventoryItemEntity_ {
    public static volatile SingularAttribute<InventoryItemEntity, Long> id;
    public static volatile SingularAttribute<InventoryItemEntity, String> uuid;
    public static volatile SingularAttribute<InventoryItemEntity, String> sku;
    public static volatile SingularAttribute<InventoryItemEntity, String> productVariantId;
    public static volatile SingularAttribute<InventoryItemEntity, String> locationId;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> onHand;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> reserved;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> inTransit;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> damaged;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> safetyStock;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> reorderPoint;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> reorderQuantity;
    public static volatile SingularAttribute<InventoryItemEntity, Integer> maxStock;
    public static volatile SingularAttribute<InventoryItemEntity, InventoryStatus> status;
    public static volatile SingularAttribute<InventoryItemEntity, LocalDateTime> lastUpdated;
    public static volatile ListAttribute<InventoryItemEntity, StockMovementEntity> movements;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String SKU = "sku";
    public static final String PRODUCT_VARIANT_ID = "productVariantId";
    public static final String LOCATION_ID = "locationId";
    public static final String ON_HAND = "onHand";
    public static final String RESERVED = "reserved";
    public static final String IN_TRANSIT = "inTransit";
    public static final String DAMAGED = "damaged";
    public static final String SAFETY_STOCK = "safetyStock";
    public static final String REORDER_POINT = "reorderPoint";
    public static final String REORDER_QUANTITY = "reorderQuantity";
    public static final String MAX_STOCK = "maxStock";
    public static final String STATUS = "status";
    public static final String LAST_UPDATED = "lastUpdated";
    public static final String MOVEMENT_ENTITIES = "movements";
}
