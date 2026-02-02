package com.product.infrastructure.entity.product.factory;

import com.grab.framework.id.IdGenerator;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductVariantOptionEntityFactory {
    private IdGenerator IdGenerator;

    public ProductVariationEntity create(ProductVariation productVariation, VariantTypeEntity type, VariantOptionEntity option) {
        ProductVariationEntity variationEntity = new ProductVariationEntity();
        variationEntity.setUuid(IdGenerator.generateId().getValue());
        variationEntity.setVariantType(type);
        variationEntity.setVariantOption(option);
        return variationEntity;
    }
}
