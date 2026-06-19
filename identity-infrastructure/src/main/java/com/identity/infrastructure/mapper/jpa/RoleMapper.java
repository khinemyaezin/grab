package com.identity.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.identity.domain.aggregate.Role;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class RoleMapper {

    @Mapping(source = "entity.uuid", target = "id")
    @Mapping(target = "authorityCodes", expression = "java(mapAuthorities(entity))")
    public abstract Role toDomain(RoleEntity entity);

    protected Set<String> mapAuthorities(RoleEntity entity) {
        if (entity == null || entity.getAuthorities() == null) return java.util.Collections.emptySet();
        return entity.getAuthorities().stream().filter(AuthorityEntity::isActive).map(AuthorityEntity::getCode).collect(Collectors.toSet());
    }
}
