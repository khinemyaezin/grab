package com.identity.infrastructure.mapper.jpa.impl;

import com.identity.domain.aggregate.User;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.mapper.jpa.UserEntityMapper;
import com.identity.infrastructure.mapper.jpa.UserJpaAssembler;
import com.identity.infrastructure.mapper.jpa.UserMapper;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class UserJpaAssemblerImpl implements UserJpaAssembler {

    private final UserEntityMapper entityMapper;
    private final UserMapper domainMapper;

    @Override
    public UserEntity buildFullEntityGraph(User user, UserEntity entity) {
        if (entity == null) {
            entity = new UserEntity();
        }
        entityMapper.toEntity(user, entity);
        return entity;
    }

    @Override
    public User toFullDomainGraph(UserEntity entity) {
        if (entity == null) return null;
        return domainMapper.toDomain(entity);
    }
}
