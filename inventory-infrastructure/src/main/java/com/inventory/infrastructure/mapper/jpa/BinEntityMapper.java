package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.entity.Bin;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.meta.BinEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import com.inventory.infrastructure.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class BinEntityMapper {

    @Mapping(ignore = true, target = BinEntity_.ID)
    @Mapping(source = "id", target = BinEntity_.UUID)
    @Mapping(source = "code", target = BinEntity_.CODE)
    @Mapping(source = "name", target = BinEntity_.NAME)
    @Mapping(source = "maxCapacity", target = BinEntity_.MAX_CAPACITY)
    @Mapping(source = "active", target = BinEntity_.ACTIVE)
    @Mapping(ignore = true, target = BinEntity_.ZONE)
    public abstract void toEntity(Bin source, @MappingTarget BinEntity destination);
}
