package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.aggregate.Location;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.entity.meta.LocationEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class LocationEntityMapper {

    @Mapping(ignore = true, target = LocationEntity_.ID)
    @Mapping(source = "id", target = LocationEntity_.UUID)
    @Mapping(source = "code", target = LocationEntity_.CODE)
    @Mapping(source = "name", target = LocationEntity_.NAME)
    @Mapping(source = "type", target = LocationEntity_.TYPE)
    @Mapping(source = "address.line1", target = LocationEntity_.STREET)
    @Mapping(source = "address.line2", target = LocationEntity_.STREET2)
    @Mapping(source = "address.city", target = LocationEntity_.CITY)
    @Mapping(source = "address.state", target = LocationEntity_.STATE)
    @Mapping(source = "address.postalCode", target = LocationEntity_.POSTAL_CODE)
    @Mapping(source = "address.country", target = LocationEntity_.COUNTRY)
    @Mapping(source = "active", target = LocationEntity_.ACTIVE)
    @Mapping(ignore = true, target = LocationEntity_.ZONES)
    public abstract void toEntity(Location source, @MappingTarget LocationEntity destination);
}
