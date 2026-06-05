package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.valueobject.Address;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class LocationMapper {

    @Mapping(source = "entity." + LocationEntity_.UUID, target = "id")
    @Mapping(source = "entity." + LocationEntity_.CODE, target = "code")
    @Mapping(source = "entity." + LocationEntity_.NAME, target = "name")
    @Mapping(source = "entity." + LocationEntity_.TYPE, target = "type")
    @Mapping(target = "address", expression = "java(mapAddress(entity))")
    @Mapping(target = "active", ignore = true)
    public abstract Location toDomain(LocationEntity entity);

    @BeforeMapping
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

    @AfterMapping
    protected void setActive(LocationEntity entity, @MappingTarget Location location) {
        if (entity.isActive()) {
            location.activate();
        } else {
            location.deactivate();
        }
    }
}
