package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RoleDeclarationStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDeclarationStateJpaRepository extends JpaRepository<RoleDeclarationStateEntity, String> {
}
