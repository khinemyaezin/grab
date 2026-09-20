package com.inventory.adapter.persistence.adapter;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.adapter.persistence.entity.BinEntity;
import com.inventory.adapter.persistence.repository.jpa.BinJpaRepository;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.model.read.BinSearchCriteria;
import com.inventory.adapter.persistence.specification.jpa.BinSearchSpecification;
import com.inventory.application.model.read.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class BinQueryAdapter implements BinQueryPort {

    private final BinJpaRepository jpaRepository;
    private final BinSearchSpecification searchSpecification;
    private final PersistenceExecutor executor;

    @Override
    public Page<BinView> queryByZoneId(String zoneId, Pageable pageable) {
        return executor.query("Bin", () -> jpaRepository.findAllByZoneId(zoneId, pageable));
    }

    @Override
    public Page<BinView> queryByZoneIdAndActive(String zoneId, boolean active, Pageable pageable) {
        return executor.query("Bin", () -> jpaRepository.findAllByZoneIdAndActive(zoneId, active, pageable));
    }

    @Override
    public Page<BinView> search(BinSearchCriteria criteria, Pageable pageable) {
        return executor.query("Bin", () -> searchSpecification.search(criteria, pageable));
    }

    @Override
    public Optional<BinView> findById(String binId) {
        return executor.query("Bin", () -> jpaRepository.findByUuid(binId).map(this::toView));
    }

    private BinView toView(BinEntity entity) {
        return new BinView(
                entity.getUuid(),
                entity.getCode(),
                entity.getName(),
                entity.getMaxCapacity(),
                entity.isActive(),
                entity.getZoneId()
        );
    }
}
