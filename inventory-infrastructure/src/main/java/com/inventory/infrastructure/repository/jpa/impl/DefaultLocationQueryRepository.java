package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationQueryRepository;
import com.inventory.infrastructure.view.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultLocationQueryRepository implements LocationQueryRepository {

    private final LocationJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Page<LocationView> queryAll(String merchantId, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllByMerchantId(merchantId, pageable));
    }

    @Override
    public Page<LocationView> queryByActive(String merchantId, Pageable pageable) {
        return executor.query("Location", () ->
                jpaRepository.findAllByMerchantIdAndActiveTrue(merchantId, pageable));
    }

    @Override
    public Page<LocationView> queryByType(String merchantId, LocationType type, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllByMerchantIdAndType(merchantId, type, pageable));
    }
}
