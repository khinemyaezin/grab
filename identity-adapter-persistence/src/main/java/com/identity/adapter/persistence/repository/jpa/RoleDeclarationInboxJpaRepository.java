package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RoleDeclarationInboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDeclarationInboxJpaRepository extends JpaRepository<RoleDeclarationInboxEntity, String> {
}
