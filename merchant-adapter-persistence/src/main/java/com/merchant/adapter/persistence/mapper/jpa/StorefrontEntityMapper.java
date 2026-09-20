package com.merchant.adapter.persistence.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StorefrontEntityMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(ignore = true, target = "channelBrand")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "merchantId", target = "merchantId")
    @Mapping(source = "name.value", target = "name")
    @Mapping(source = "slug.value", target = "slug")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "lifecycleReason.value", target = "lifecycleReason")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "version", target = "version")
    public abstract void toEntity(Storefront source, @MappingTarget StorefrontEntity destination);
}
