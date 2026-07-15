package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.repository.jpa.ProductVariantQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.specification.jpa.ProductVariantSearchSpecification;
import com.catalog.infrastructure.view.ProductVariantView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class ProductVariantQueryRepositoryImpl implements ProductVariantQueryRepository {
    private static final Logger log = Loggers.getLogger(ProductVariantQueryRepositoryImpl.class);

    private final ProductVariantSearchSpecification productVariantSearchSpecification;
    private final PersistenceExecutor executor;

    @Override
    public Page<ProductVariantView> search(ProductSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching product variant views with pageable={}", pageable);

        Page<ProductVariantView> page = executor.query("ProductVariant", () ->
                productVariantSearchSpecification.search(criteria, pageable));

        log.debug("Product variant search returned {} elements", page.getTotalElements());
        return page;
    }
}
