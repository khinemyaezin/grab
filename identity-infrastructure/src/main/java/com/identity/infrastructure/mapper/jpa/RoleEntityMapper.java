package com.identity.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.Role;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class RoleEntityMapper {

    @Mapping(source = "id", target = "uuid")
    @Mapping(target = "authorities", ignore = true)
    public abstract void toEntity(Role source, @MappingTarget RoleEntity destination);
}
