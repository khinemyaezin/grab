package com.identity.infrastructure.repository.jpa.impl;

import com.identity.domain.repository.AuthorityRepository;
import com.identity.infrastructure.repository.jpa.AuthorityJpaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultAuthorityRepository implements AuthorityRepository {

    private final AuthorityJpaRepository jpaRepository;

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }
}
