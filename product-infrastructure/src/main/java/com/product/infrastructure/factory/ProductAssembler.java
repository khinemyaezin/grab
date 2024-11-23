package com.product.infrastructure.factory;

import com.product.domain.entity.product.Product;
import com.product.domain.entity.product_variant.ProductVariant;
import com.product.domain.entity.product_variant.ProductVariation;
import com.product.domain.entity.product_variant.VariationCompositeKey;
import com.product.domain.entity.variant_option.VariantOption;
import com.product.domain.entity.variant_type.VariantType;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductAssembler {
    public Product assemble(ProductEntity jpaEntity) {
        Product product = createProduct(jpaEntity);

        Map<VariationCompositeKey, ProductVariation> variationMap = createVariationMap(jpaEntity.getProductVariants());

        // Build product variants
        for (ProductVariantEntity variantEntity : jpaEntity.getProductVariants()) {
            List<ProductVariation> productVariations = createVariations(variantEntity, variationMap);
            ProductVariant variant = createVariant(jpaEntity.getUuid(), variantEntity, productVariations);
            product.addVariant(variant);
        }

        return product;
    }

    public Map<VariationCompositeKey, ProductVariation> createVariationMap(List<ProductVariantEntity> productVariants) {
        Map<VariantType, VariantType> variantTypes = new HashMap<>();

        // Build variationMap directly
        return productVariants.stream()
                .flatMap(variant -> variant.getProductVariantOptions().stream())
                .collect(Collectors.toMap(
                        optionEntity -> new VariationCompositeKey(
                                optionEntity.getVariantOptionValue(),
                                optionEntity.getVariantTypeValue()
                        ),
                        optionEntity -> {
                            VariantType variantType = new VariantType(
                                    optionEntity.getVariantType().getUuid(),
                                    optionEntity.getVariantTypeValue()
                            );
                            VariantOption variantOption = new VariantOption(
                                    optionEntity.getVariantOption().getUuid(),
                                    optionEntity.getVariantOptionValue(),
                                    variantType
                            );

                            variantTypes.computeIfAbsent(variantType, k -> variantType).addOption(variantOption);
                            return new ProductVariation(variantOption);
                        },
                        (existing, replacement) -> existing // Handle duplicate keys
                ));
    }

    public Product createProduct(ProductEntity jpaEntity) {
        return new Product(
                jpaEntity.getUuid(),
                jpaEntity.getName(),
                jpaEntity.getCategory().getUuid()
        );
    }

    public ProductVariant createVariant(String parentId, ProductVariantEntity variantEntity, List<ProductVariation> variations) {
        return new ProductVariant(
                variantEntity.getUuid(),
                parentId,
                variantEntity.getSku(),
                variations
        );
    }

    public List<ProductVariation> createVariations(ProductVariantEntity variantEntity, Map<VariationCompositeKey, ProductVariation> variationMap) {
        return variantEntity.getProductVariantOptions().stream()
                .map(optionEntity -> variationMap.get(
                        new VariationCompositeKey(
                                optionEntity.getVariantOptionValue(),
                                optionEntity.getVariantTypeValue()
                        )
                )).toList();
    }
}
