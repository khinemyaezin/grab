package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class ZoneMapper {

    @Mapping(source = "entity." + ZoneEntity_.UUID, target = "id")
    @Mapping(source = "entity." + ZoneEntity_.CODE, target = "code")
    @Mapping(source = "entity." + ZoneEntity_.NAME, target = "name")
    @Mapping(source = "entity." + ZoneEntity_.TYPE, target = "type")
    @Mapping(source = "entity." + ZoneEntity_.ACTIVE, target = "active")
    public abstract Zone toDomain(ZoneEntity entity);
}
