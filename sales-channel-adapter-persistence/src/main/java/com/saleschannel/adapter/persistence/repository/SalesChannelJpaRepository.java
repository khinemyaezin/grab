package com.saleschannel.adapter.persistence.repository;

import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface SalesChannelJpaRepository extends JpaRepository<SalesChannelEntity, Long> {
    @Override
    @EntityGraph(attributePaths = "supportedRegions")
    Optional<SalesChannelEntity> findById(Long id);

    @EntityGraph(attributePaths = "supportedRegions")
    Optional<SalesChannelEntity> findByUuid(String uuid);

    @EntityGraph(attributePaths = "supportedRegions")
    Optional<SalesChannelEntity> findByTypeAndMerchantId(ChannelType type, String merchantId);

    @EntityGraph(attributePaths = "supportedRegions")
    Optional<SalesChannelEntity> findFirstByType(ChannelType type);

    boolean existsByTypeAndMerchantId(ChannelType type, String merchantId);

    boolean existsByType(ChannelType type);
}
