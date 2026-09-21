package com.identity.adapter.persistence.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.User;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class UserEntityMapper {
    @Mapping(ignore = true, target = "id")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "email.value", target = "email")
    @Mapping(target = "passwordHash", expression = "java(source.getPasswordHash().map(com.identity.domain.valueobject.HashedPassword::hash).orElse(null))")
    public abstract void toEntity(User source, @MappingTarget UserEntity destination);
}
