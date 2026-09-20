package com.inventory.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.LocationType;
import com.inventory.adapter.persistence.entity.LocationEntity;
import com.inventory.adapter.persistence.repository.jpa.LocationJpaRepository;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.model.read.LocationSearchCriteria;
import com.inventory.adapter.persistence.specification.jpa.LocationSearchSpecification;
import com.inventory.application.model.read.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class LocationQueryAdapter implements LocationQueryPort {

    private final LocationJpaRepository jpaRepository;
    private final LocationSearchSpecification searchSpecification;
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

    @Override
    public Page<LocationView> search(LocationSearchCriteria criteria, Pageable pageable) {
        return executor.query("Location", () -> searchSpecification.search(criteria, pageable));
    }

    @Override
    public Optional<LocationView> findById(String locationId) {
        return executor.query("Location", () -> jpaRepository.findByUuid(locationId).map(this::toView));
    }

    @Override
    public Optional<LocationView> findByCode(String code) {
        return executor.query("Location", () -> jpaRepository.findByCode(code).map(this::toView));
    }

    private LocationView toView(LocationEntity entity) {
        return new LocationView(
                entity.getUuid(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                entity.getStreet(),
                entity.getStreet2(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getCountry(),
                entity.isActive()
        );
    }
}
