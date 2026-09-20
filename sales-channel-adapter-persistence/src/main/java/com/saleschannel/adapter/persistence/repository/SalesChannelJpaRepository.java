package com.saleschannel.adapter.persistence.repository;

import com.saleschannel.domain.enums.ChannelType;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesChannelJpaRepository extends JpaRepository<SalesChannelEntity, Long> {
    Optional<SalesChannelEntity> findByUuid(String uuid);

    Optional<SalesChannelEntity> findByTypeAndMerchantId(ChannelType type, String merchantId);

    Optional<SalesChannelEntity> findFirstByType(ChannelType type);

    boolean existsByTypeAndMerchantId(ChannelType type, String merchantId);

    boolean existsByType(ChannelType type);
}
