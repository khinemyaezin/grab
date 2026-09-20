package com.identity.adapter.persistence.mapper.jpa.impl;

import com.identity.domain.aggregate.User;
import com.identity.adapter.persistence.entity.UserEntity;
import com.identity.adapter.persistence.mapper.jpa.UserEntityMapper;
import com.identity.adapter.persistence.mapper.jpa.UserJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.UserMapper;
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
