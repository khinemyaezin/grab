package com.identity.adapter.persistence.adapter;

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

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }

    @Override
    public Set<String> findActiveCodes(Set<String> codes) {
        return jpaRepository.findByCodeInAndActiveTrue(codes).stream()
                .map(AuthorityEntity::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void upsertAll(List<Authority> authorities) {
        for (Authority authority : authorities) {
            jpaRepository.upsertByCode(authority.code(), authority.name(), authority.description());
        }
    }
}
