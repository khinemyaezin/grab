package com.product.infrastructure.mapper.jpa;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.mapper.CommonMapper;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import lombok.AllArgsConstructor;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = CentralMapperConfig.class,uses = {CommonMapper.class})
public class ProductVariationMapper {
    @Autowired
    private IdGenerator idGenerator;

    public ProductVariation toDomain(ProductVariationEntity productVariationEntity) {
        Id typeId = idGenerator.generateId(productVariationEntity.getId().getVariantTypeUuid());
        Id optionId = idGenerator.generateId(productVariationEntity.getId().getVariantOptionUuid());
        return new ProductVariation(
                productVariationEntity.getVariantOptionValue(),
                optionId,
                productVariationEntity.getVariantTypeValue(),
                typeId
        );
    }
}
