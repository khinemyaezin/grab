package com.identity.adapter.persistence.adapter;

import com.grab.framework.mapper.IdMapper;
import com.grab.framework.support.PersistenceExecutor;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.repository.jpa.AuthorityJpaRepository;
import com.identity.domain.aggregate.Authority;
import com.identity.domain.port.outbound.AuthorityRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AuthorityRepositoryAdapter implements AuthorityRepository {

    private final AuthorityJpaRepository jpaRepository;
    private final IdMapper ids;
    private final PersistenceExecutor executor;

    @Override
    public Set<Authority> findActiveByCodes(Set<String> codes) {
        return executor.query("Authority", () -> {
            List<AuthorityEntity> entities = jpaRepository.findByCodeInAndActiveTrue(codes);
            return entities.stream()
                    .filter(AuthorityEntity::isEffective)
                    .map(this::toDomain)
                    .collect(Collectors.toUnmodifiableSet());
        });
    }

    @Override
    public Set<String> findCodesByModule(String moduleKey) {
        return executor.query("Authority", () -> {
            List<AuthorityEntity> entities = jpaRepository.findByCategory(moduleKey);
            return entities.stream()
                    .map(AuthorityEntity::getCode)
                    .collect(Collectors.toUnmodifiableSet());
        });
    }

    @Override
    public Set<String> findCodesOwnedByOtherModules(String moduleKey) {
        return executor.query("Authority", () -> {
            List<AuthorityEntity> entities = jpaRepository.findByCategoryNot(moduleKey);
            return entities.stream()
                    .map(AuthorityEntity::getCode)
                    .collect(Collectors.toUnmodifiableSet());
        });
    }

    @Override
    public void upsertAll(List<Authority> authorities) {
        executor.command("Authority", () -> {
            for (Authority authority : authorities) {
                Optional<AuthorityEntity> existing = jpaRepository.findByCode(authority.getCode());
                existing.ifPresent(entity -> {
                    if (!authority.getCategory().equals(entity.getCategory())) {
                        throw new IllegalArgumentException(
                                "authority code is already owned by module: " + authority.getCode()
                        );
                    }
                });
                AuthorityEntity entity = existing.orElseGet(AuthorityEntity::new);
                if (entity.getUuid() == null) {
                    entity.setUuid(ids.map(authority.getId()));
                    entity.setCode(authority.getCode());
                    entity.setActive(true);
                }
                entity.setCategory(authority.getCategory());
                if (entity.getOwnerKey() == null) {
                    entity.setOwnerKey(authority.getCategory());
                }
                entity.setName(authority.getName());
                entity.setDescription(authority.getDescription());
                jpaRepository.save(entity);
            }
            return null;
        });
    }

    @Override
    public void retireMissing(String moduleKey, Set<String> declaredCodes) {
        executor.command("Authority", () -> {
            List<AuthorityEntity> entities = jpaRepository.findByCategory(moduleKey);
            entities.stream()
                    .filter(authority -> !declaredCodes.contains(authority.getCode()))
                    .filter(AuthorityEntity::isActive)
                    .forEach(authority -> {
                        authority.setActive(false);
                        jpaRepository.save(authority);
                    });
            return null;
        });
    }

    @Override
    public void retireCodes(String moduleKey, Set<String> codes) {
        executor.command("Authority", () -> {
            List<AuthorityEntity> entities = jpaRepository.findByCategory(moduleKey);
            entities.stream()
                    .filter(authority -> codes.contains(authority.getCode()))
                    .filter(AuthorityEntity::isActive)
                    .forEach(authority -> {
                        authority.setActive(false);
                        jpaRepository.save(authority);
                    });
            return null;
        });
    }

    private Authority toDomain(AuthorityEntity entity) {
        return Authority.rehydrate(
                ids.map(entity.getUuid()),
                entity.getCode(),
                entity.getCategory(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive()
        );
    }
}
