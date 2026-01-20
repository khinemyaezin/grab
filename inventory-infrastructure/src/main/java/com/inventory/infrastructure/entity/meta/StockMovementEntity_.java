package com.inventory.infrastructure.entity.meta;

import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.domain.enums.StockMovementType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(StockMovementEntity.class)
public class StockMovementEntity_ {
    public static volatile SingularAttribute<StockMovementEntity, Long> id;
    public static volatile SingularAttribute<StockMovementEntity, String> uuid;
    public static volatile SingularAttribute<StockMovementEntity, InventoryItemEntity> inventoryItem;
    public static volatile SingularAttribute<StockMovementEntity, StockMovementType> type;
    public static volatile SingularAttribute<StockMovementEntity, Integer> quantity;
    public static volatile SingularAttribute<StockMovementEntity, Integer> quantityBefore;
    public static volatile SingularAttribute<StockMovementEntity, Integer> quantityAfter;
    public static volatile SingularAttribute<StockMovementEntity, String> referenceId;
    public static volatile SingularAttribute<StockMovementEntity, LocalDateTime> createdAt;
    public static volatile SingularAttribute<StockMovementEntity, String> createdBy;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String INVENTORY_ITEM = "inventoryItem";
    public static final String TYPE = "type";
    public static final String QUANTITY = "quantity";
    public static final String QUANTITY_BEFORE = "quantityBefore";
    public static final String QUANTITY_AFTER = "quantityAfter";
    public static final String REFERENCE_ID = "referenceId";
    public static final String CREATED_AT = "createdAt";
    public static final String CREATED_BY = "createdBy";
}
