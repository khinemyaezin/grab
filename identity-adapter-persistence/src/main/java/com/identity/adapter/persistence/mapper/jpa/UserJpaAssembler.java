package com.identity.adapter.persistence.mapper.jpa;

import com.identity.domain.aggregate.User;
import com.identity.adapter.persistence.entity.UserEntity;

public interface UserJpaAssembler {
    UserEntity buildFullEntityGraph(User user, UserEntity entity);
    User toFullDomainGraph(UserEntity entity);
}
