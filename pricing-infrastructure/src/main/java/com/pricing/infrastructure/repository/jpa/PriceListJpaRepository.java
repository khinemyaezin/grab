package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PriceListEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceListJpaRepository extends JpaRepository<PriceListEntity, Long> {
    @EntityGraph(attributePaths = {"rules", "prices", "prices.rules", "prices.priceSet"})
    Optional<PriceListEntity> findByUuid(String uuid);

    void deleteByUuid(String uuid);
}
