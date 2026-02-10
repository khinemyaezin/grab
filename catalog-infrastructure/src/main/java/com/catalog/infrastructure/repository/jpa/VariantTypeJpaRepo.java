package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantTypeJpaRepo extends JpaRepository<VariantTypeEntity, Long> {
    Optional<VariantTypeEntity> findByUuid(String uuid);
}
