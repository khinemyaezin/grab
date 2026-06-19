package com.identity.infrastructure.repository.jpa;

import com.identity.infrastructure.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCode(String code);
    List<RoleEntity> findByCodeIn(Collection<String> codes);
}
