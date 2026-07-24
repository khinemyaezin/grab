package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PriceSetEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceSetJpaRepository extends JpaRepository<PriceSetEntity, Long> {
    @EntityGraph(attributePaths = {"prices", "prices.rules"})
    Optional<PriceSetEntity> findByUuid(String uuid);

    void deleteByUuid(String uuid);

    boolean existsByUuid(String uuid);
}
