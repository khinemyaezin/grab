package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PriceAdjustmentEntity;
import com.pricing.infrastructure.repository.EntityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingAdjustmentJpaRepository extends EntityRepository<PriceAdjustmentEntity, Long>, JpaRepository<PriceAdjustmentEntity, Long> {
}
