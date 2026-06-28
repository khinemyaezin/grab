package com.identity.infrastructure.repository.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.repository.PlatformRepository;
import com.identity.infrastructure.entity.PlatformRoleEntity;
import com.identity.infrastructure.repository.jpa.PlatformJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultPlatformRepository implements PlatformRepository {
    private final PlatformJpaRepository platforms;
    private final IdMapper ids;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Platform> findByCode(String code) {
        return executor.query("Platform", () -> platforms.findByCode(code).map(entity -> new Platform(
                ids.map(entity.getUuid()),
                entity.getCode(),
                entity.getName(),
                entity.isActive(),
                entity.getPlatformRoles().stream()
                        .filter(PlatformRoleEntity::isActive)
                        .filter(platformRole -> platformRole.getRole().isActive())
                        .map(platformRole -> platformRole.getRole().getCode())
                        .collect(Collectors.toSet())
        )));
    }
}
