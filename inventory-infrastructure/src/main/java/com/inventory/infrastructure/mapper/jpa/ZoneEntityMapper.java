package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.aggregate.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class ZoneEntityMapper {

    @Mapping(ignore = true, target = ZoneEntity_.ID)
    @Mapping(source = "id", target = ZoneEntity_.UUID)
    @Mapping(source = "locationId", target = ZoneEntity_.LOCATION_ID)
    @Mapping(source = "code", target = ZoneEntity_.CODE)
    @Mapping(source = "name", target = ZoneEntity_.NAME)
    @Mapping(source = "type", target = ZoneEntity_.TYPE)
    @Mapping(source = "active", target = ZoneEntity_.ACTIVE)
    public abstract void toEntity(Zone source, @MappingTarget ZoneEntity destination);
}
