package com.identity.adapter.persistence.repository.jpa;

import com.identity.adapter.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByCode(String code);
    List<RoleEntity> findByCodeIn(Collection<String> codes);
    List<RoleEntity> findTop5ByNameStartingWithIgnoreCase(String name);

}
