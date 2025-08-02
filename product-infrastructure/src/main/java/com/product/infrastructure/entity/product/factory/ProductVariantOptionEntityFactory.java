package com.product.infrastructure.entity.product.factory;

import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ProductVariantOptionEntityFactory {
    private IdGenerator IdGenerator;

    public ProductVariationEntity create(ProductVariation productVariation, Optional<VariantTypeEntity> type, Optional<VariantOptionEntity> option) {
        ProductVariationEntity variationEntity = new ProductVariationEntity();
        type.ifPresent(variationEntity::setVariantType);
        option.ifPresent(variationEntity::setVariantOption);

        variationEntity.setUuid(IdGenerator.generateId().getValue());
        variationEntity.setVariantOptionValue(productVariation.getOptionName());
        variationEntity.setVariantTypeValue(productVariation.getTypeName());
        return variationEntity;
    }
}
