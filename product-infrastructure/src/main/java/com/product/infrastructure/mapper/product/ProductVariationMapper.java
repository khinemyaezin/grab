package com.product.infrastructure.mapper.product;

import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.aggregate.product.VariationCompositeKey;
import com.product.infrastructure.common.CommonId;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantOptionEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductVariationMapper {

    public ProductVariation reconstruct(ProductVariantOptionEntity optionEntity, Map<VariationCompositeKey, ProductVariation> variationMap) {
        return variationMap.get(
                new VariationCompositeKey(
                        optionEntity.getVariantOptionValue(),
                        optionEntity.getVariantTypeValue()
                ));
    }

    public Map<VariationCompositeKey, ProductVariation> createVariationMap(List<ProductVariantEntity> productVariants) {
        Map<VariantType, VariantType> variantTypes = new HashMap<>();

        return productVariants.stream()
                .flatMap(variant -> variant.getProductVariantOptions().stream())
                .collect(Collectors.toMap(
                        optionEntity -> new VariationCompositeKey(
                                optionEntity.getVariantOptionValue(),
                                optionEntity.getVariantTypeValue()
                        ),
                        optionEntity -> {
                            VariantType variantType = new VariantType(
                                    new CommonId(optionEntity.getVariantType().getUuid()),
                                    optionEntity.getVariantTypeValue()
                            );

                            // if key variantType is new, variantTypeInMap has the same reference with key variantType.
                            // if it is already existed, they have different reference
                            VariantType variantTypeInMap = variantTypes.computeIfAbsent(variantType, k -> variantType);
                            VariantOption variantOption = new VariantOption(
                                    new CommonId(optionEntity.getVariantOption().getUuid()),
                                    optionEntity.getVariantOptionValue(),
                                    variantTypeInMap
                            );
                            variantTypeInMap.addOption(variantOption);
                            return new ProductVariation(variantOption);
                        },
                        (existing, replacement) -> existing // Handle duplicate keys
                ));
    }
}
