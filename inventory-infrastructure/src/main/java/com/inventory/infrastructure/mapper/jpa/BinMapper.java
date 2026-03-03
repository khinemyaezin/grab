package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.Bin;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.meta.BinEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class BinMapper {

    @Mapping(source = "entity." + BinEntity_.UUID, target = "id")
    @Mapping(source = "entity." + BinEntity_.CODE, target = "code")
    @Mapping(source = "entity." + BinEntity_.NAME, target = "name")
    @Mapping(source = "entity." + BinEntity_.MAX_CAPACITY, target = "maxCapacity")
    @Mapping(source = "entity." + BinEntity_.ACTIVE, target = "active")
    public abstract Bin toDomain(BinEntity entity);
}
