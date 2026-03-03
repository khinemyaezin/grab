package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.entity.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import com.inventory.infrastructure.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class ZoneEntityMapper {

    @Mapping(ignore = true, target = ZoneEntity_.ID)
    @Mapping(source = "id", target = ZoneEntity_.UUID)
    @Mapping(source = "code", target = ZoneEntity_.CODE)
    @Mapping(source = "name", target = ZoneEntity_.NAME)
    @Mapping(source = "type", target = ZoneEntity_.TYPE)
    @Mapping(source = "active", target = ZoneEntity_.ACTIVE)
    @Mapping(ignore = true, target = ZoneEntity_.LOCATION)
    @Mapping(ignore = true, target = ZoneEntity_.BINS)
    public abstract void toEntity(Zone source, @MappingTarget ZoneEntity destination);
}
