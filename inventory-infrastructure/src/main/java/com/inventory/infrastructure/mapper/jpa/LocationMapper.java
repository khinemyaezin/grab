package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.valueobject.Address;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class LocationMapper {

    @Mapping(source = "entity." + LocationEntity_.UUID, target = "id")
    @Mapping(source = "entity." + LocationEntity_.CODE, target = "code")
    @Mapping(source = "entity." + LocationEntity_.NAME, target = "name")
    @Mapping(source = "entity." + LocationEntity_.TYPE, target = "type")
    @Mapping(target = "address", expression = "java(mapAddress(entity))")
    @Mapping(source = "entity." + LocationEntity_.ACTIVE, target = "active")
    public abstract Location toDomain(LocationEntity entity);

    protected Address mapAddress(LocationEntity entity) {
        if (entity == null) return null;
        return new Address(
                entity.getStreet(),
                entity.getStreet2(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry()
        );
    }
}
