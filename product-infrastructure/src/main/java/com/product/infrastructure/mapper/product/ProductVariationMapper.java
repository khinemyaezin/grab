package com.product.infrastructure.mapper.product;

import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class ProductVariationMapper {
    private final IdGenerator idGenerator;

    public ProductVariation toDomain(ProductVariationEntity productVariantOptionEntity) {
        if(Objects.isNull(productVariantOptionEntity.getVariantOption())) {
            return new ProductVariation(productVariantOptionEntity.getVariantOptionValue(),null, productVariantOptionEntity.getVariantTypeValue());
        } else {
            return new ProductVariation(productVariantOptionEntity.getVariantOptionValue(),
                    idGenerator.generateId(productVariantOptionEntity.getVariantOption().getUuid()),
                    productVariantOptionEntity.getVariantTypeValue());
        }
    }
}
