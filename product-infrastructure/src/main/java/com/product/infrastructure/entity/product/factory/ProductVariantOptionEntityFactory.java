package com.product.infrastructure.entity.product.factory;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
public class ProductVariantOptionEntityFactory {

    public ProductVariantOptionEntity create(ProductVariation inputOption, Optional<VariantTypeEntity> type, Optional<VariantOptionEntity> option) {
        ProductVariantOptionEntity.ProductVariantOptionEntityBuilder variantOptionEntityBuilder = ProductVariantOptionEntity.builder();
        type.ifPresent(variantOptionEntityBuilder::variantType);
        option.ifPresent(variantOptionEntityBuilder::variantOption);

        variantOptionEntityBuilder.uuid(UUID.randomUUID().toString());
        variantOptionEntityBuilder.variantOptionValue(inputOption.getVariantOption().getName());
        variantOptionEntityBuilder.variantTypeValue(inputOption.getVariantOption().getVariantType().getName());
        return variantOptionEntityBuilder.build();
    }
}
