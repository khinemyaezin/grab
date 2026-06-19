package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.ExternalIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalIdentityJpaRepository extends JpaRepository<ExternalIdentityEntity, Long> {
    Optional<ExternalIdentityEntity> findByIssuerAndSubject(String issuer, String subject);
}
