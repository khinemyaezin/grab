package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.valueobject.InventoryQuantity;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.meta.InventoryItemEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class InventoryItemMapper {

    @Mapping(source = "entity." + InventoryItemEntity_.UUID, target = "id")
    @Mapping(source = "entity." + InventoryItemEntity_.SKU, target = "sku")
    @Mapping(source = "entity." + InventoryItemEntity_.PRODUCT_VARIANT_ID, target = "productVariantId")
    @Mapping(source = "entity." + InventoryItemEntity_.LOCATION_ID, target = "locationId")
    @Mapping(target = "quantity", expression = "java(mapQuantity(entity))")
    @Mapping(target = "reorderConfig", expression = "java(mapReorderConfig(entity))")
    @Mapping(source = "entity." + InventoryItemEntity_.STATUS, target = "status")
    public abstract InventoryItem toDomain(InventoryItemEntity entity);

    protected InventoryQuantity mapQuantity(InventoryItemEntity entity) {
        if (entity == null) return null;
        return new InventoryQuantity(
                entity.getOnHand(),
                entity.getReserved(),
                entity.getInTransit(),
                entity.getDamaged()
        );
    }

    protected ReorderConfig mapReorderConfig(InventoryItemEntity entity) {
        if (entity == null) return null;
        return new ReorderConfig(
                entity.getSafetyStock(),
                entity.getReorderPoint(),
                entity.getReorderQuantity(),
                entity.getMaxStock()
        );

    }
}
