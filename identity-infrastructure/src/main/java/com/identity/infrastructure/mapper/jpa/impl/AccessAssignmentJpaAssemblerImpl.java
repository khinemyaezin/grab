package com.identity.infrastructure.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.AccessAssignment;
import com.identity.domain.valueobject.AccessScope;
import com.identity.infrastructure.entity.AccessAssignmentEntity;
import com.identity.infrastructure.mapper.jpa.AccessAssignmentJpaAssembler;
import com.identity.infrastructure.repository.jpa.PlatformRoleJpaRepository;
import com.identity.infrastructure.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessAssignmentJpaAssemblerImpl implements AccessAssignmentJpaAssembler {
    private final UserJpaRepository users;
    private final PlatformRoleJpaRepository platformRoles;
    private final IdMapper ids;

    @Override
    public AccessAssignmentEntity buildFullEntityGraph(
            AccessAssignment source,
            AccessAssignmentEntity destination
    ) {
        AccessAssignmentEntity entity = destination == null ? new AccessAssignmentEntity() : destination;
        entity.setUuid(source.getId().getValue());
        entity.setUser(users.findByUuid(source.getUserId().getValue()).orElseThrow());
        entity.setPlatformRole(platformRoles
                .findByPlatform_CodeAndRole_CodeAndActiveTrue(source.getPlatformCode(), source.getRoleCode())
                .orElseThrow());
        entity.setScopeType(source.getScope().type());
        entity.setScopeId(source.getScope().scopeId());
        entity.setStatus(source.getStatus());
        entity.setAssignedBy(source.getAssignedBy() == null ? null : source.getAssignedBy().getValue());
        entity.setCreatedAt(source.getCreatedAt());
        entity.setUpdatedAt(source.getUpdatedAt());
        entity.setExpiresAt(source.getExpiresAt());
        return entity;
    }

    @Override
    public AccessAssignment toDomain(AccessAssignmentEntity source) {
        return new AccessAssignment(
                ids.map(source.getUuid()),
                ids.map(source.getUser().getUuid()),
                source.getPlatformRole().getPlatform().getCode(),
                source.getPlatformRole().getRole().getCode(),
                new AccessScope(source.getScopeType(), source.getScopeId()),
                source.getStatus(),
                ids.map(source.getAssignedBy()),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getExpiresAt()
        );
    }
}
