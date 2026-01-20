package com.inventory.infrastructure.mapper;

import com.grab.framework.mapper.CommonMapper;
import com.inventory.domain.entity.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.entity.meta.ZoneEntity_;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {BinMapper.class, CommonMapper.class})
public interface ZoneEntityMapper {

    @Mapping(target = ZoneEntity_.ID, ignore = true)
    @Mapping(target = ZoneEntity_.UUID, source = "id")
    @Mapping(target = ZoneEntity_.LOCATION, ignore = true)
    @Mapping(target = ZoneEntity_.BINS, ignore = true)
    ZoneEntity toEntity(Zone domain);
}
