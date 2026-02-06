package com.product.infrastructure.mapper.jpa;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = CentralMapperConfig.class,uses = {CommonMapper.class})
public abstract class ProductMapper {
    @Autowired
    private IdGenerator idGenerator;

    public Product toDomain(ProductEntity productJpaEntity, List<ProductVariant> variants) {
        Id productId = idGenerator.generateId(productJpaEntity.getUuid());
        String productName = productJpaEntity.getName();
        Id categoryId = idGenerator.generateId(productJpaEntity.getCategoryId());

        return new Product(productId,productName, categoryId, variants);
    }


}
