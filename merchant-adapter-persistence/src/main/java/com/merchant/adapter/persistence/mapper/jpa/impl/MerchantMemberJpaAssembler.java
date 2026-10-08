package com.merchant.adapter.persistence.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.merchant.adapter.persistence.entity.MerchantMemberEntity;
import com.merchant.adapter.persistence.mapper.jpa.MerchantMemberEntityMapper;
import com.merchant.domain.aggregate.MerchantMember;
import com.merchant.domain.valueobject.MerchantRole;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;

@RequiredArgsConstructor
public class MerchantMemberJpaAssembler {
    private final MerchantMemberEntityMapper entityMapper;
    private final IdMapper ids;

    public MerchantMemberEntity toEntity(MerchantMember source, MerchantMemberEntity destination) {
        MerchantMemberEntity entity = destination == null ? new MerchantMemberEntity() : destination;
        entityMapper.toEntity(source, entity);
        if (source.getRole() != null) {
            entity.setRole(source.getRole().name());
            entity.setAuthorities(new HashSet<>(source.getRole().authorities()));
        }
        return entity;
    }

    public MerchantMember toDomain(MerchantMemberEntity source) {
        MerchantRole domainRole = MerchantRole.of(source.getRole(), source.getAuthorities());
        return new MerchantMember(
                ids.map(source.getUuid()),
                ids.map(source.getMerchantId()),
                ids.map(source.getUserId()),
                domainRole,
                source.getStatus(),
                source.getInvitedBy() == null ? null : ids.map(source.getInvitedBy()),
                source.getInvitationExpiresAt(),
                source.getJoinedAt(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }
}
