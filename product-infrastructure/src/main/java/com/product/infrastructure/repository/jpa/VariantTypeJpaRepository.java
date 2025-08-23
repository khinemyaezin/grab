package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantTypeJpaRepository extends JpaRepository<VariantTypeEntity, Long> {
    Optional<VariantTypeEntity> findByUuid(String uuid);
}
