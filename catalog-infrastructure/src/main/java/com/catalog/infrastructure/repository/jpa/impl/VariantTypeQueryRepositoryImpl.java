package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.repository.jpa.VariantTypeJpaRepo;
import com.catalog.infrastructure.repository.jpa.VariantTypeQueryRepository;
import com.catalog.infrastructure.view.VariantTypeView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class VariantTypeQueryRepositoryImpl implements VariantTypeQueryRepository {
    private static final Logger log = Loggers.getLogger(VariantTypeQueryRepositoryImpl.class);

    private final VariantTypeJpaRepo variantTypeJpaRepo;
    private final PersistenceExecutor executor;

    @Override
    public List<VariantTypeView> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Collections.emptyList();
        }
        log.debug("Querying variant types by name={}", name);
        return executor.query("VariantType", () ->
                variantTypeJpaRepo.searchByName(name));
    }
}
