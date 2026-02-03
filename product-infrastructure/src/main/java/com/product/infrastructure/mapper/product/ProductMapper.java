package com.product.infrastructure.mapper.product;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductMapper {
    private final IdGenerator idGenerator;
    private final ProductVariantMapper variantMapper;

    public Product toDomain(ProductEntity productJpaEntity) {
        Id productId = idGenerator.generateId(productJpaEntity.getUuid());
        String productName = productJpaEntity.getName();
        Id categoryId = idGenerator.generateId(productJpaEntity.getCategoryId());
        Product product = Product.create(productId, productName, categoryId);

        for (ProductVariantEntity variantEntity : productJpaEntity.getProductVariants()) {
            ProductVariant variant = variantMapper.toDomain(product, variantEntity);
            boolean isAdded = product.addVariant(variant);
            if(!isAdded) {
                throw new IllegalArgumentException("Unable to add variant");
            }
        }
        return product;
    }


}
