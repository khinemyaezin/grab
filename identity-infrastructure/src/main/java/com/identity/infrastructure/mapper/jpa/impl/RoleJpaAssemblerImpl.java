package com.identity.infrastructure.mapper.jpa.impl;

import com.identity.domain.aggregate.Role;
import com.identity.infrastructure.entity.RoleEntity;
import com.identity.infrastructure.mapper.jpa.RoleEntityMapper;
import com.identity.infrastructure.mapper.jpa.RoleJpaAssembler;
import com.identity.infrastructure.mapper.jpa.RoleMapper;
import com.identity.infrastructure.repository.jpa.AuthorityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleJpaAssemblerImpl implements RoleJpaAssembler {

    private final RoleEntityMapper entityMapper;
    private final RoleMapper domainMapper;
    private final AuthorityJpaRepository authorities;

    @Override
    public RoleEntity buildFullEntityGraph(Role role, RoleEntity entity) {
        if (entity == null) {
            entity = new RoleEntity();
        }
        entityMapper.toEntity(role, entity);
        entity.setAuthorities(role.getAuthorityCodes().stream()
                .map(code -> authorities.findByCode(code).orElseThrow())
                .collect(Collectors.toSet()));
        return entity;
    }

    @Override
    public Role toFullDomainGraph(RoleEntity entity) {
        if (entity == null) return null;
        return domainMapper.toDomain(entity);
    }
}
