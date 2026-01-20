package com.inventory.infrastructure.mapper;

import com.grab.framework.mapper.CommonMapper;
import com.inventory.domain.aggregate.Location;
import com.inventory.infrastructure.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ZoneMapper.class, CommonMapper.class})
public interface LocationEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    @Mapping(target = "street", source = "address.line1")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "postalCode", source = "address.postalCode")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "zones", ignore = true)
    LocationEntity toEntity(Location domain);
}
