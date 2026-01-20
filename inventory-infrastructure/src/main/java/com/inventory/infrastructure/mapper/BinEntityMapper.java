package com.inventory.infrastructure.mapper;

import com.grab.framework.mapper.CommonMapper;
import com.inventory.domain.entity.Bin;
import com.inventory.infrastructure.entity.BinEntity;
import com.inventory.infrastructure.entity.meta.BinEntity_;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = CommonMapper.class)
public interface BinEntityMapper {

    @Mapping(target = BinEntity_.ID, ignore = true)
    @Mapping(target = BinEntity_.UUID, source = "id")
    @Mapping(target = "zone", ignore = true)
    BinEntity toEntity(Bin domain);
}
