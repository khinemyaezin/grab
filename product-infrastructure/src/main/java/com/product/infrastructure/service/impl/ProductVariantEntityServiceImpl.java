package com.product.infrastructure.service.impl;

import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.mapper.product_variant.ProductVariantEntityMapper;
import com.product.infrastructure.service.ProductVariantOptionEntityService;
import com.product.infrastructure.service.ProductVariantEntityService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductVariantEntityServiceImpl implements ProductVariantEntityService {
    private final ProductVariantEntityMapper productVariantEntityMapper;
    private final ProductVariantOptionEntityService productVariantOptionService;


    public void updateVariants(ProductEntity productEntity, List<ProductVariant> productVariants) {
        Map<String, ProductVariant> inputVariantsMap = productVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity(), (e1, e2) -> e1, LinkedHashMap::new));

        mergeAndRemoveVariants(productEntity, inputVariantsMap);
        addNewVariants(productEntity, inputVariantsMap);
    }

    private void mergeAndRemoveVariants(ProductEntity productEntity, Map<String, ProductVariant> inputVariantsMap) {
        for (ProductVariantEntity existingVariant : productEntity.getProductVariants()) {
            ProductVariant inputVariant = inputVariantsMap.get(existingVariant.getUuid());

            if (inputVariant != null) {
                productVariantEntityMapper.map(inputVariant, existingVariant);
                productVariantOptionService.updateVariations(existingVariant, inputVariant.getVariations());
                inputVariantsMap.remove(existingVariant.getUuid());
            } else {
                productEntity.removeVariant(existingVariant);
            }
        }
    }

    private void addNewVariants(ProductEntity productEntity, Map<String, ProductVariant> remainingVariantsMap) {
        for (ProductVariant inputVariant : remainingVariantsMap.values()) {
            ProductVariantEntity newVariant = new ProductVariantEntity();
            productVariantEntityMapper.map(inputVariant, newVariant);
            productVariantOptionService.updateVariations(newVariant, inputVariant.getVariations());
            productEntity.addVariant(newVariant);
        }
    }
}
