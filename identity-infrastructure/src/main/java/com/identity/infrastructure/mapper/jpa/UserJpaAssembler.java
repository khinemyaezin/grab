package com.identity.infrastructure.mapper.jpa;

import com.identity.domain.aggregate.User;
import com.identity.infrastructure.entity.UserEntity;

public interface UserJpaAssembler {
    UserEntity buildFullEntityGraph(User user, UserEntity entity);
    User toFullDomainGraph(UserEntity entity);
}
