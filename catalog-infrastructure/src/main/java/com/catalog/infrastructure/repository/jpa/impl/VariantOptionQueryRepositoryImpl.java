package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.repository.jpa.VariantOptionJpaRepo;
import com.catalog.infrastructure.repository.jpa.VariantOptionQueryRepository;
import com.catalog.infrastructure.view.VariantOptionView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class VariantOptionQueryRepositoryImpl implements VariantOptionQueryRepository {
    private static final Logger log = Loggers.getLogger(VariantOptionQueryRepositoryImpl.class);

    private final VariantOptionJpaRepo variantOptionQueryJpaRepo;
    private final PersistenceExecutor executor;

    @Override
    public List<VariantOptionView> findAllByUuidIn(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Querying variant options for uuids={}", uuids);
        return executor.query("VariantOption", () -> variantOptionQueryJpaRepo.findAllByUuidIn(uuids));
    }

    @Override
    public List<VariantOptionView> findByNameAndTypeId(String name, String typeUuid) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }
        log.debug("Querying variant options by name={}", name);
        return executor.query("VariantOption", () ->
                variantOptionQueryJpaRepo.findByNameContainingIgnoreCaseAndTypeId(name, typeUuid));
    }
}
