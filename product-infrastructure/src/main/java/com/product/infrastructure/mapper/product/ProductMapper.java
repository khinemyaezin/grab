package com.product.infrastructure.mapper.product;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductMapper {
    private final IdGenerator idGenerator;
    private final ProductVariantMapper variantMapper;

    public Product toDomain(ProductEntity productJpaEntity) {
        Id productId = idGenerator.generateId(productJpaEntity.getUuid());
        String productName = productJpaEntity.getName();
        Id categoryId = idGenerator.generateId(productJpaEntity.getCategory().getUuid());
        Product product = new Product(productId, productName, categoryId);

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
