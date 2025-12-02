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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

    private void mergeAndRemoveVariantOptions(ProductVariantEntity productVariantEntity, Set<ProductVariation> updatedVariantOptions) {
        Set<ProductVariationEntity> optionsToRemove = new HashSet<>();

        // First pass: identify what to remove
        for (ProductVariationEntity optionEntity : productVariantEntity.getProductVariations()) {
            ProductVariation oldVariantOption = productVariationMapper.toDomain(optionEntity);
            if (updatedVariantOptions.contains(oldVariantOption)) {
                updatedVariantOptions.remove(oldVariantOption);
            } else {
                optionsToRemove.add(optionEntity);
            }
        }

        // Second pass: perform removal
        for (ProductVariationEntity optionToRemove : optionsToRemove) {
            productVariantEntity.removeProductVariantOption(optionToRemove);
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
