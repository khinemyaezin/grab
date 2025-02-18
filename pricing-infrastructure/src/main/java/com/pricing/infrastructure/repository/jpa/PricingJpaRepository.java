package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PricingEntity;
import com.pricing.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingJpaRepository extends EntityRepository<PricingEntity,Long>, JpaRepository<PricingEntity, Long> {
    Optional<PricingEntity> findByUuid(String uuid);
    Optional<PricingEntity> findByProduct(String productId);
}
