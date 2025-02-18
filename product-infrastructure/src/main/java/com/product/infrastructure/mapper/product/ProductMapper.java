package com.product.infrastructure.mapper.product;

import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.infrastructure.common.CommonId;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@AllArgsConstructor
public class ProductMapper {
    private final ProductFactory productFactory;
    private final ProductVariationMapper variationMapper;
    private final ProductVariantMapper variantMapper;

    public Product reconstruct(ProductEntity jpaEntity) {
        Product product = productFactory.createProduct(new CommonId(jpaEntity.getUuid()), jpaEntity.getName(), new CommonId(jpaEntity.getCategory().getUuid()));

        Map<VariationCompositeKey, ProductVariation> variationMap = variationMapper.createVariationMap(jpaEntity.getProductVariants());

        // Build product variants
        for (ProductVariantEntity variantEntity : jpaEntity.getProductVariants()) {
            List<ProductVariation> productVariations = createVariations(variantEntity, variationMap);
            ProductVariant variant = variantMapper.reconstruct(product, variantEntity, productVariations);
            product.addVariant(variant);
        }

        return product;
    }

    private List<ProductVariation> createVariations(ProductVariantEntity variantEntity, Map<VariationCompositeKey, ProductVariation> variationMap) {
        return variantEntity.getProductVariantOptions().stream()
                .map(option-> variationMapper.reconstruct(option,variationMap))
                .toList();
    }
}
