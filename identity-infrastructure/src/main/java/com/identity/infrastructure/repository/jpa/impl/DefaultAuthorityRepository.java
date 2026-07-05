package com.identity.infrastructure.repository.jpa.impl;

import com.identity.domain.repository.AuthorityRepository;
import com.identity.infrastructure.entity.AuthorityEntity;
import com.identity.infrastructure.repository.jpa.AuthorityJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefaultAuthorityRepository implements AuthorityRepository {

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
}
