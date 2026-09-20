package com.merchant.adapter.persistence.repository.jpa;

import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorefrontChannelBrandJpaRepository extends JpaRepository<StorefrontChannelBrandEntity, Long> {
    Optional<StorefrontChannelBrandEntity> findByStorefrontId(Long storefrontId);

    boolean existsByStorefrontId(Long storefrontId);
}
