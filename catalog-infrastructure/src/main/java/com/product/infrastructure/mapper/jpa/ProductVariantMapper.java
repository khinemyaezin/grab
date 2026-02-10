package com.product.infrastructure.mapper.jpa;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariantStatus;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = CentralMapperConfig.class,uses = {CommonMapper.class})
public class ProductVariantMapper {
    @Autowired
    private IdGenerator idGenerator;

    public ProductVariant toDomain(ProductVariantEntity variantEntity, List<ProductVariation> variations) {
        return new ProductVariant(
                idGenerator.generateId(variantEntity.getUuid()),
                variantEntity.getSku(),
                toProductVariantStatus(variantEntity.getStatus()),
                variations);
    }

    private ProductVariantStatus toProductVariantStatus(String status) {
        return ProductVariantStatus.valueOf(status);
    }

}
