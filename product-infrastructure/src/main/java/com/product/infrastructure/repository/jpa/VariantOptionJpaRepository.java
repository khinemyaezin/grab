package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantOptionJpaRepository extends JpaRepository<VariantOptionEntity, Long> {
    Optional<VariantOptionEntity> findByUuid(String uuid);

}
