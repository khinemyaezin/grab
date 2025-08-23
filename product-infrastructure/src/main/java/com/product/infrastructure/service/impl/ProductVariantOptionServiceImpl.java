package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantOptionEntityFactory;
import com.product.infrastructure.mapper.product.ProductVariationMapper;
import com.product.infrastructure.repository.jpa.VariantOptionJpaRepository;
import com.product.infrastructure.service.ProductVariantOptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ProductVariantOptionServiceImpl implements ProductVariantOptionService {
    private final ProductVariantOptionEntityFactory productVariantOptionEntityFactory;
    private final ProductVariationMapper productVariationMapper;
    private final VariantOptionJpaRepository variantOptionJpaRepository;

    @Override
    public void updateVariations(ProductVariantEntity productVariantEntity, Set<ProductVariation> updatedVariantOptions) {

        mergeAndRemoveVariantOptions(productVariantEntity, updatedVariantOptions);
        addNewVariantOptions(productVariantEntity, updatedVariantOptions);
    }

    private void mergeAndRemoveVariantOptions(ProductVariantEntity productVariantEntity, Set<ProductVariation> inputOptionsMap) {
        for (ProductVariationEntity optionEntity : productVariantEntity.getProductVariations()) {
            ProductVariation variation = productVariationMapper.toDomain(optionEntity);
            if (inputOptionsMap.contains(variation)) {
                inputOptionsMap.remove(variation);
            } else {
                productVariantEntity.removeProductVariantOption(optionEntity);
            }
        }
    }

    private void addNewVariantOptions(ProductVariantEntity productVariantEntity, Set<ProductVariation> productVariations) {
        for (ProductVariation productVariation : productVariations) {
            Optional<VariantOptionEntity> option = Objects.isNull(productVariation.getOptionId())
                    ? Optional.empty() : this.variantOptionJpaRepository.findByUuid(productVariation.getOptionId().getValue());

            Optional<VariantTypeEntity> type = option.map( VariantOptionEntity::getVariantType);
            ProductVariationEntity newOption = this.productVariantOptionEntityFactory.create(productVariation,type,option);
            productVariantEntity.addProductVariantOption(newOption);
        }
    }
}
