package com.inventory.infrastructure.entity.meta;

import com.inventory.domain.enums.InventoryReservationStatus;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.LocalDateTime;

@StaticMetamodel(InventoryReservationEntity.class)
public class InventoryReservationEntity_ {
    public static volatile SingularAttribute<InventoryReservationEntity, Long> id;
    public static volatile SingularAttribute<InventoryReservationEntity, String> uuid;
    public static volatile SingularAttribute<InventoryReservationEntity, String> inventoryItemUuid;
    public static volatile SingularAttribute<InventoryReservationEntity, String> orderId;
    public static volatile SingularAttribute<InventoryReservationEntity, String> orderLineId;
    public static volatile SingularAttribute<InventoryReservationEntity, Integer> quantity;
    public static volatile SingularAttribute<InventoryReservationEntity, InventoryReservationStatus> status;
    public static volatile SingularAttribute<InventoryReservationEntity, LocalDateTime> expiresAt;
    public static volatile SingularAttribute<InventoryReservationEntity, String> idempotencyKey;
    public static volatile SingularAttribute<InventoryReservationEntity, LocalDateTime> createdAt;
    public static volatile SingularAttribute<InventoryReservationEntity, LocalDateTime> updatedAt;

    public static final String ID = "id";
    public static final String UUID = "uuid";
    public static final String INVENTORY_ITEM_UUID = "inventoryItemUuid";
    public static final String ORDER_ID = "orderId";
    public static final String ORDER_LINE_ID = "orderLineId";
    public static final String QUANTITY = "quantity";
    public static final String STATUS = "status";
    public static final String EXPIRES_AT = "expiresAt";
    public static final String IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
}
