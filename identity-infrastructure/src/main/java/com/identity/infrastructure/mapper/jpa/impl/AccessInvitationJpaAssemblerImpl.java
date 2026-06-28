package com.identity.infrastructure.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.AccessInvitation;
import com.identity.domain.valueobject.AccessScope;
import com.identity.domain.valueobject.Email;
import com.identity.infrastructure.entity.AccessInvitationEntity;
import com.identity.infrastructure.mapper.jpa.AccessInvitationJpaAssembler;
import com.identity.infrastructure.repository.jpa.PlatformRoleJpaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessInvitationJpaAssemblerImpl implements AccessInvitationJpaAssembler {
    private final PlatformRoleJpaRepository platformRoles;
    private final IdMapper ids;

    @Override
    public AccessInvitationEntity buildFullEntityGraph(
            AccessInvitation source,
            AccessInvitationEntity destination
    ) {
        AccessInvitationEntity entity = destination == null ? new AccessInvitationEntity() : destination;
        entity.setUuid(source.getId().getValue());
        entity.setInviteeEmail(source.getInviteeEmail().value());
        entity.setPlatformRole(platformRoles
                .findByPlatform_CodeAndRole_CodeAndActiveTrue(source.getPlatformCode(), source.getRoleCode())
                .orElseThrow());
        entity.setScopeKey(source.getScope().key().value());
        entity.setScopeId(source.getScope().scopeId());
        entity.setTokenHash(source.getTokenHash());
        entity.setInvitedBy(source.getInvitedBy().getValue());
        entity.setStatus(source.getStatus());
        entity.setCreatedAt(source.getCreatedAt());
        entity.setUpdatedAt(source.getUpdatedAt());
        entity.setExpiresAt(source.getExpiresAt());
        entity.setAcceptedBy(source.getAcceptedBy() == null ? null : source.getAcceptedBy().getValue());
        return entity;
    }

    @Override
    public AccessInvitation toDomain(AccessInvitationEntity source) {
        return new AccessInvitation(
                ids.map(source.getUuid()),
                new Email(source.getInviteeEmail()),
                source.getPlatformRole().getPlatform().getCode(),
                source.getPlatformRole().getRole().getCode(),
                AccessScope.from(source.getScopeKey(), source.getScopeId()),
                source.getTokenHash(),
                ids.map(source.getInvitedBy()),
                source.getStatus(),
                source.getCreatedAt(),
                source.getExpiresAt(),
                ids.map(source.getAcceptedBy()),
                source.getUpdatedAt()
        );
    }
}
