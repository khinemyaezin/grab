package com.pricing.infrastructure.repository.jpa;

import com.pricing.infrastructure.entity.PricePreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricePreferenceJpaRepository extends JpaRepository<PricePreferenceEntity, Long> {
    Optional<PricePreferenceEntity> findByUuid(String uuid);

    void deleteByUuid(String uuid);
}
