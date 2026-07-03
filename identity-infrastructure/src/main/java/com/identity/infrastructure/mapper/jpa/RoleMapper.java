package com.identity.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.aggregate.Role;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.entity.RoleEntity;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RoleMapper {
    private final IdMapper ids;

    public Role toDomain(RoleEntity entity) {
        Set<String> authorityCodes = entity.getAuthorities().stream()
                .filter(AuthorityEntity::isActive)
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());
        return Role.rehydrate(
                ids.map(entity.getUuid()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getKind(),
                entity.isActive(),
                entity.isAssignable(),
                authorityCodes
        );
    }
}
