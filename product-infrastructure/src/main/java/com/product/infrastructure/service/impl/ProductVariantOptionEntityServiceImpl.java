package com.product.infrastructure.service.impl;

import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.product_variant.VariationCompositeKey;
import com.product.infrastructure.entity.helper.ProductVariationCompositeKey;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantOptionEntity;
import com.product.infrastructure.repository.variant_option.VariantOptionRepository;
import com.product.infrastructure.repository.variant_type.VariantTypeRepository;
import com.product.infrastructure.service.ProductVariantOptionEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductVariantOptionEntityServiceImpl implements ProductVariantOptionEntityService {
    private final VariantTypeRepository variantTypeRepository;
    private final VariantOptionRepository variantOptionRepository;

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
            ProductVariantOptionEntity newOption = createFrom(inputOption);
            productVariantEntity.addProductVariantOption(newOption);
        }
    }

    private ProductVariantOptionEntity createFrom(ProductVariation inputOption) {
        ProductVariantOptionEntity.ProductVariantOptionEntityBuilder variantOptionEntityBuilder = ProductVariantOptionEntity.builder();
        variantTypeRepository.findByUuid(inputOption.getVariantOption().getVariantType().getId())
                .ifPresent(variantOptionEntityBuilder::variantType);
        variantOptionRepository.findByUuid(inputOption.getVariantOption().getId())
                .ifPresent(variantOptionEntityBuilder::variantOption);

        variantOptionEntityBuilder.uuid(UUID.randomUUID().toString());
        variantOptionEntityBuilder.variantOptionValue(inputOption.getVariantOption().getName());
        variantOptionEntityBuilder.variantTypeValue(inputOption.getVariantOption().getVariantType().getName());
        return variantOptionEntityBuilder.build();
    }

    private VariationCompositeKey extractKey(ProductVariantOptionEntity e){
        return new VariationCompositeKey(e.getVariantOptionValue(),e.getVariantTypeValue());
    }
}
