package com.product.infrastructure.mapper.product;

import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.common.CommonId;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductVariantMapper {

    public ProductVariant reconstruct(Product parent, ProductVariantEntity variantEntity, List<ProductVariation> variations) {
        return parent.getId().map(
                id-> new ProductVariant(
                        new CommonId(variantEntity.getUuid()),
                        id,
                        variantEntity.getSku(),
                        variations
                )

        ).orElseThrow();
    }
}
