package com.catalog.adapter.persistence.adapter;

import com.catalog.adapter.persistence.repository.VariantTypeJpaRepo;
import com.catalog.application.port.outbound.VariantTypeQueryPort;
import com.catalog.application.readmodel.VariantTypeView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class VariantTypeQueryAdapter implements VariantTypeQueryPort {
    private static final Logger log = Loggers.getLogger(VariantTypeQueryAdapter.class);

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
