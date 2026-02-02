package com.product.infrastructure.mapper.product;

import com.grab.framework.id.IdGenerator;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductVariationMapper {
    private final IdGenerator idGenerator;

    public ProductVariation toDomain(ProductVariationEntity productVariantOptionEntity) {
        return new ProductVariation(productVariantOptionEntity.getVariantOption().getName(),
                idGenerator.generateId(productVariantOptionEntity.getVariantOption().getUuid()),
                productVariantOptionEntity.getVariantOption().getVariantType().getName(),
                idGenerator.generateId(productVariantOptionEntity.getVariantOption().getVariantType().getUuid()));
    }
}
