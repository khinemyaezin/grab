package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.repository.jpa.ZoneQueryRepository;
import com.inventory.infrastructure.view.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultZoneQueryRepository implements ZoneQueryRepository {

    private final ZoneJpaRepository jpaRepository;
    private final PersistenceExecutor executor;

    @Override
    public Page<ZoneView> queryByLocationId(String locationId, Pageable pageable) {
        return executor.query("Zone", () -> jpaRepository.findAllByLocationId(locationId, pageable));
    }

    @Override
    public Page<ZoneView> queryByLocationIdAndActive(String locationId, boolean active, Pageable pageable) {
        return executor.query("Zone", () -> jpaRepository.findAllByLocationIdAndActive(locationId, active, pageable));
    }
}
