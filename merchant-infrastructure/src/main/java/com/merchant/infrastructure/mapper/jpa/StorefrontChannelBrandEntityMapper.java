package com.merchant.infrastructure.mapper.jpa;

import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.infrastructure.entity.StorefrontChannelBrandEntity;
import com.merchant.infrastructure.entity.meta.StorefrontChannelBrandEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class StorefrontChannelBrandEntityMapper {

    @Mapping(ignore = true, target = StorefrontChannelBrandEntity_.STOREFRONT_ID)
    @Mapping(ignore = true, target = "storefront")
    @Mapping(source = "salesChannelId", target = StorefrontChannelBrandEntity_.SALES_CHANNEL_ID)
    public abstract void toEntity(StorefrontChannelBrand source, @MappingTarget StorefrontChannelBrandEntity destination);
}
