package com.identity.infrastructure.mapper.jpa.impl;

import com.identity.domain.aggregate.User;
import com.identity.infrastructure.entity.UserEntity;
import com.identity.infrastructure.mapper.jpa.UserEntityMapper;
import com.identity.infrastructure.mapper.jpa.UserJpaAssembler;
import com.identity.infrastructure.mapper.jpa.UserMapper;
import com.identity.infrastructure.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class UserJpaAssemblerImpl implements UserJpaAssembler {

    private final UserEntityMapper entityMapper;
    private final UserMapper domainMapper;
    private final RoleJpaRepository roles;

    @Override
    public UserEntity buildFullEntityGraph(User user, UserEntity entity) {
        if (entity == null) {
            entity = new UserEntity();
        }
        entityMapper.toEntity(user, entity);
        entity.setRoles(new HashSet<>(roles.findByCodeIn(user.getRoleCodes())));
        return entity;
    }

    @Override
    public User toFullDomainGraph(UserEntity entity) {
        if (entity == null) return null;
        return domainMapper.toDomain(entity);
    }
}
