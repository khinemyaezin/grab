package com.identity.infrastructure.mapper.jpa;

import com.identity.domain.aggregate.Role;
import com.identity.infrastructure.entity.RoleEntity;

public interface RoleJpaAssembler {
    RoleEntity buildFullEntityGraph(Role role, RoleEntity entity);
    Role toFullDomainGraph(RoleEntity entity);
}
