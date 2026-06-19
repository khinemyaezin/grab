package com.identity.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.identity.domain.aggregate.User;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class UserMapper {

    @Mapping(source = "entity.uuid", target = "id")
    @Mapping(target = "email", expression = "java(new com.identity.domain.valueobject.Email(entity.getEmail()))")
    @Mapping(target = "passwordHash", expression = "java(entity.getPasswordHash() == null ? null : new com.identity.domain.valueobject.HashedPassword(entity.getPasswordHash()))")
    @Mapping(target = "roleCodes", expression = "java(mapRoles(entity))")
    public abstract User toDomain(UserEntity entity);

    protected Set<String> mapRoles(UserEntity entity) {
        if (entity == null || entity.getRoles() == null) return java.util.Collections.emptySet();
        return entity.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toSet());
    }
}
