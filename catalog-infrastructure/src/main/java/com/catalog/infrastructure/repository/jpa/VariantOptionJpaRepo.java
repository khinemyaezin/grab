package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.entity.entity.VariantOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantOptionJpaRepo extends JpaRepository<VariantOptionEntity, Long> {
    Optional<VariantOptionEntity> findByUuid(String uuid);

}
