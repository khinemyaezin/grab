package com.inventory.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.adapter.persistence.entity.ZoneEntity;
import com.inventory.adapter.persistence.repository.jpa.ZoneJpaRepository;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.ZoneSearchCriteria;
import com.inventory.adapter.persistence.specification.jpa.ZoneSearchSpecification;
import com.inventory.application.model.read.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class ZoneQueryAdapter implements ZoneQueryPort {

    private final ZoneJpaRepository jpaRepository;
    private final ZoneSearchSpecification searchSpecification;
    private final PersistenceExecutor executor;

    @Override
    public Page<ZoneView> queryByLocationId(String locationId, Pageable pageable) {
        return executor.query("Zone", () -> jpaRepository.findAllByLocationId(locationId, pageable));
    }

    @Override
    public Page<ZoneView> queryByLocationIdAndActive(String locationId, boolean active, Pageable pageable) {
        return executor.query("Zone", () -> jpaRepository.findAllByLocationIdAndActive(locationId, active, pageable));
    }

    @Override
    public Page<ZoneView> search(ZoneSearchCriteria criteria, Pageable pageable) {
        return executor.query("Zone", () -> searchSpecification.search(criteria, pageable));
    }

    @Override
    public Optional<ZoneView> findById(String zoneId) {
        return executor.query("Zone", () -> jpaRepository.findByUuid(zoneId).map(this::toView));
    }

    private ZoneView toView(ZoneEntity entity) {
        return new ZoneView(
                entity.getUuid(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.isActive(),
                entity.getLocationId()
        );
    }
}
