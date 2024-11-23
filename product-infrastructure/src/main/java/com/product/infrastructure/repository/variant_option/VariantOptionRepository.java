package com.product.infrastructure.repository.variant_option;

import com.product.infrastructure.entity.product.entity.VariantOptionEntity;
import com.product.infrastructure.entity.product.entity.VariantTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariantOptionRepository extends JpaRepository<VariantOptionEntity, Long> {
    Optional<VariantOptionEntity> findByUuid(String uuid);

}
