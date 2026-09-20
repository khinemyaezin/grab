package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.meta.ProductPublicationEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class ProductPublicationEntityMapper {

    @Mapping(ignore = true, target = ProductPublicationEntity_.VARIANT_ID)
    @Mapping(source = "salesChannelId", target = ProductPublicationEntity_.SALES_CHANNEL_ID)
    public abstract void toEntity(ProductPublication source, @MappingTarget ProductPublicationEntity destination);
}
