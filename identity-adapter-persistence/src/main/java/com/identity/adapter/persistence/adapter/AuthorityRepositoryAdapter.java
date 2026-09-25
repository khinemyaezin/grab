package com.identity.adapter.persistence.adapter;

import com.grab.framework.mapper.IdMapper;
import com.identity.domain.port.outbound.AuthorityRepository;
import com.identity.adapter.persistence.entity.AuthorityEntity;
import com.identity.adapter.persistence.repository.jpa.AuthorityJpaRepository;
import com.identity.domain.model.Authority;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AuthorityRepositoryAdapter implements AuthorityRepository {

    private final AuthorityJpaRepository jpaRepository;
    private final IdMapper ids;

    @Override
    public Set<Authority> findActiveByCodes(Set<String> codes) {
        return jpaRepository.findByCodeInAndActiveTrue(codes).stream()
                .map(this::toDomain)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void upsertAll(List<Authority> authorities) {
        for (Authority authority : authorities) {
            jpaRepository.upsertByCode(
                    ids.map(authority.getId()),
                    authority.getCode(),
                    authority.getCategory(),
                    authority.getName(),
                    authority.getDescription()
            );
        }
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
