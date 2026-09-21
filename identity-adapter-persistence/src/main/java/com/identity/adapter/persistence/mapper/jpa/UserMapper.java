package com.identity.adapter.persistence.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.identity.domain.aggregate.User;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class UserMapper {

    @Mapping(source = "entity.uuid", target = "id")
    @Mapping(target = "email", expression = "java(new com.identity.domain.valueobject.Email(entity.getEmail()))")
    @Mapping(target = "passwordHash", expression = "java(entity.getPasswordHash() == null ? null : new com.identity.domain.valueobject.HashedPassword(entity.getPasswordHash()))")
    public abstract User toDomain(UserEntity entity);
}
