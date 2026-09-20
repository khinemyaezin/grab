package com.identity.adapter.persistence.mapper.jpa.impl;

import com.identity.domain.aggregate.Role;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.mapper.jpa.RoleEntityMapper;
import com.identity.adapter.persistence.mapper.jpa.RoleJpaAssembler;
import com.identity.adapter.persistence.mapper.jpa.RoleMapper;
import com.identity.adapter.persistence.repository.jpa.AuthorityJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

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
