package com.product.infrastructure.mapper.product;

import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ProductVariantMapper {
    private final IdGenerator idGenerator;
    private final ProductVariationMapper variationMapper;

    public ProductVariant toDomain(Product product, ProductVariantEntity variantEntity) {
        return new ProductVariant(
                idGenerator.generateId(variantEntity.getUuid()),
                product.getId(),
                variantEntity.getSku(),
                getVariations(variantEntity));
    }

    private List<ProductVariation> getVariations(ProductVariantEntity variantEntity) {
        return variantEntity.getProductVariations().stream()
                .map(variationMapper::toDomain)
                .toList();
    }
}
