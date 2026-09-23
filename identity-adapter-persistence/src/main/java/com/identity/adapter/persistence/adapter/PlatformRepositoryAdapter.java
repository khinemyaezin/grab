package com.identity.adapter.persistence.adapter;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.domain.aggregate.Platform;
import com.identity.domain.port.outbound.PlatformRepository;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.entity.PlatformEntity;
import com.identity.adapter.persistence.entity.PlatformRoleEntity;
import com.identity.adapter.persistence.entity.RoleEntity;
import com.identity.adapter.persistence.repository.jpa.AuthorityJpaRepository;
import com.identity.adapter.persistence.repository.jpa.PlatformJpaRepository;
import com.identity.adapter.persistence.repository.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PlatformRepositoryAdapter implements PlatformRepository {
    private final PlatformJpaRepository platforms;
    private final RoleJpaRepository roles;
    private final AuthorityJpaRepository authorities;
    private final IdMapper ids;
    private final IdGenerator idGenerator;
    private final PersistenceExecutor executor;

    @Override
    public Optional<Platform> findByCode(String code) {
        return executor.query("Platform", () -> platforms.findByCode(code).map(this::toDomain));
    }

    @Override
    public Set<Platform> findByRoleCode(String roleCode) {
        return executor.query("Platform", () -> platforms
                .findDistinctByPlatformRoles_Role_CodeAndPlatformRoles_ActiveTrue(roleCode)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public Platform save(Platform platform) {
        return executor.command("Platform", () -> {
            PlatformEntity entity = platforms.findByCode(platform.getCode()).orElseThrow();
            reconcileRoles(platform, entity);
            Set<AuthorityEntity> supportedAuthorities = new LinkedHashSet<>(
                    authorities.findByCodeInAndActiveTrue(platform.getAuthorityCodes())
            );
            entity.setAuthorities(supportedAuthorities);
            PlatformEntity saved = platforms.save(entity);
            return toDomain(saved);
        });
    }

    private void reconcileRoles(Platform source, PlatformEntity destination) {
        Set<String> defaultRoles = source.getDefaultRoles();
        Map<String, PlatformRoleEntity> existingByCode = destination.getPlatformRoles().stream()
                .collect(Collectors.toMap(
                        platformRole -> platformRole.getRole().getCode(),
                        platformRole -> platformRole,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        for (String roleCode : source.getRoleCodes()) {
            PlatformRoleEntity existing = existingByCode.remove(roleCode);
            boolean isDefault = defaultRoles.contains(roleCode);
            if (existing != null) {
                existing.setActive(true);
                existing.setDefault(isDefault);
                continue;
            }
            RoleEntity role = roles.findByCode(roleCode).orElseThrow();
            PlatformRoleEntity platformRole = new PlatformRoleEntity();
            platformRole.setUuid(idGenerator.generateId().getValue());
            platformRole.setPlatform(destination);
            platformRole.setRole(role);
            platformRole.setActive(true);
            platformRole.setDefault(isDefault);
            destination.getPlatformRoles().add(platformRole);
        }
        existingByCode.values().forEach(platformRole -> {
            platformRole.setActive(false);
            platformRole.setDefault(false);
        });
    }

    private Platform toDomain(PlatformEntity entity) {
        Set<String> roleCodes = entity.getPlatformRoles().stream()
                .filter(PlatformRoleEntity::isActive)
                .filter(platformRole -> platformRole.getRole().isActive())
                .map(platformRole -> platformRole.getRole().getCode())
                .collect(Collectors.toSet());
        Set<String> authorityCodes = entity.getAuthorities().stream()
                .filter(AuthorityEntity::isActive)
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toSet());
        Set<String> defaultRoles = entity.getPlatformRoles().stream()
                .filter(PlatformRoleEntity::isActive)
                .filter(PlatformRoleEntity::isDefault)
                .filter(platformRole -> platformRole.getRole().isActive())
                .map(platformRole -> platformRole.getRole().getCode())
                .collect(Collectors.toSet());
        return new Platform(
                ids.map(entity.getUuid()),
                entity.getCode(),
                entity.getName(),
                entity.isActive(),
                roleCodes,
                authorityCodes,
                defaultRoles
        );
    }
}
