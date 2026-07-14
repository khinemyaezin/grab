package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.infrastructure.repository.jpa.BinJpaRepository;
import com.inventory.infrastructure.repository.jpa.BinQueryRepository;
import com.inventory.infrastructure.specification.jpa.BinSearchCriteria;
import com.inventory.infrastructure.specification.jpa.BinSearchSpecification;
import com.inventory.infrastructure.view.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DefaultBinQueryRepository implements BinQueryRepository {

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
}
