package com.product.infrastructure.service.impl;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.aggregate.product.VariationCompositeKey;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import com.product.infrastructure.entity.product.factory.ProductVariantOptionEntityFactory;
import com.product.infrastructure.repository.jpa.VariantOptionJpaRepository;
import com.product.infrastructure.repository.jpa.VariantTypeJpaRepository;
import com.product.infrastructure.service.ProductVariantOptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductVariantOptionServiceImpl implements ProductVariantOptionService {
    private final ProductVariantOptionEntityFactory productVariantOptionEntityFactory;
    private final VariantTypeJpaRepository variantTypeJpaRepository;
    private final VariantOptionJpaRepository variantOptionJpaRepository;

    @Override
    public void updateVariations(ProductVariantEntity productVariantEntity, Set<ProductVariation> updatedVariantOptions) {
        Map<VariationCompositeKey, ProductVariation> inputOptionsMap = updatedVariantOptions.stream()
                .collect(Collectors.toMap( v-> new VariationCompositeKey(v.getVariantOption().getName(),v.getVariantOption().getVariantType().getName()), Function.identity()));

        mergeAndRemoveVariantOptions(productVariantEntity, inputOptionsMap);
        addNewVariantOptions(productVariantEntity, inputOptionsMap);
    }

    private void mergeAndRemoveVariantOptions(ProductVariantEntity productVariantEntity, Map<VariationCompositeKey, ProductVariation> inputOptionsMap) {
        for (ProductVariantOptionEntity existingOption : productVariantEntity.getProductVariantOptions()) {
            VariationCompositeKey key = extractKey(existingOption);
            ProductVariation inputOption = inputOptionsMap.get(key);

            if (Objects.nonNull(inputOption)) {
                inputOptionsMap.remove(key);
            } else {
                productVariantEntity.removeProductVariantOption(existingOption);
            }
        }
    }

    private void addNewVariantOptions(ProductVariantEntity productVariantEntity, Map<VariationCompositeKey, ProductVariation> remainingOptionsMap) {
        for (ProductVariation inputOption : remainingOptionsMap.values()) {
            Optional<VariantTypeEntity> type = inputOption.getVariantOption().getVariantType().getId()
                    .flatMap(uuid-> this.variantTypeJpaRepository.findByUuid(uuid.getValue()));

            Optional<VariantOptionEntity> option = inputOption.getVariantOption().getId()
                    .flatMap(uuid-> this.variantOptionJpaRepository.findByUuid(uuid.getValue()));

            ProductVariantOptionEntity newOption = this.productVariantOptionEntityFactory.create(inputOption,type,option);
            productVariantEntity.addProductVariantOption(newOption);
        }
    }

    private VariationCompositeKey extractKey(ProductVariantOptionEntity e){
        return new VariationCompositeKey(e.getVariantOptionValue(),e.getVariantTypeValue());
    }
}
