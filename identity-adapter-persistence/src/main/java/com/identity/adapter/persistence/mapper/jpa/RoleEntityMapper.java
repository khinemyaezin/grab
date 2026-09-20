package com.identity.adapter.persistence.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.Role;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class RoleEntityMapper {

    @Mapping(source = "id", target = "uuid")
    public abstract void toEntity(Role source, @MappingTarget RoleEntity destination);
}
