package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.specification.jpa.ProductSearchSpecification;
import com.catalog.infrastructure.view.ProductView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private static final Logger log = Loggers.getLogger(ProductQueryRepositoryImpl.class);

    private final ProductSearchSpecification productSearchSpecification;
    private final PersistenceExecutor executor;

    @Override
    public Page<ProductView> search(ProductSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching product views with pageable={}", pageable);

        Page<ProductView> page = executor.query("Product", () ->
                productSearchSpecification.search(criteria, pageable));

        log.debug("Product search returned {} elements", page.getTotalElements());
        return page;
    }
}
