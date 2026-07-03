package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.AuthorityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface AuthorityJpaRepository extends JpaRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByCode(String code);

    List<AuthorityEntity> findByCodeInAndActiveTrue(Collection<String> codes);
}
