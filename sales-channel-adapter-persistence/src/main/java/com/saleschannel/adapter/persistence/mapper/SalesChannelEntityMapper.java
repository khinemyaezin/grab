package com.saleschannel.adapter.persistence.mapper;

import com.grab.framework.mapper.IdMapper;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SalesChannelEntityMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "merchantId", target = "merchantId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "version", target = "version")
    public abstract void toEntity(SalesChannel source, @MappingTarget SalesChannelEntity destination);
}
