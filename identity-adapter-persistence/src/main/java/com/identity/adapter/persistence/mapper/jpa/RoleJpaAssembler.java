package com.identity.adapter.persistence.mapper.jpa;

import com.identity.domain.aggregate.Role;
import com.identity.adapter.persistence.entity.RoleEntity;

public interface RoleJpaAssembler {
    RoleEntity buildFullEntityGraph(Role role, RoleEntity entity);
    Role toFullDomainGraph(RoleEntity entity);
}
