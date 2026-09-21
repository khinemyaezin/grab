package com.pricing.adapter.persistence.repository.jpa;

import com.pricing.adapter.persistence.entity.PriceListEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceListJpaRepository extends JpaRepository<PriceListEntity, Long> {
    @EntityGraph(attributePaths = {"rules", "prices", "prices.rules", "prices.priceSet"})
    Optional<PriceListEntity> findByUuid(String uuid);

    void deleteByUuid(String uuid);

    @EntityGraph(attributePaths = {"rules", "prices", "prices.rules", "prices.priceSet"})
    List<PriceListEntity> findAllByOrderByCreatedAtAsc();
}
