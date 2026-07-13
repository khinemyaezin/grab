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
    public Page<LocationView> queryAll(String sellerId, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllBySellerId(sellerId, pageable));
    }

    @Override
    public Page<LocationView> queryByActive(String sellerId, Pageable pageable) {
        return executor.query("Location", () ->
                jpaRepository.findAllBySellerIdAndActiveTrue(sellerId, pageable));
    }

    @Override
    public Page<LocationView> queryByType(String sellerId, LocationType type, Pageable pageable) {
        return executor.query("Location", () -> jpaRepository.findAllBySellerIdAndType(sellerId, type, pageable));
    }
}
