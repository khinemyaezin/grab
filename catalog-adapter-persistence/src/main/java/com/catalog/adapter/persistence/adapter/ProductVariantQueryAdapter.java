package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.ProductVariantQueryPort;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.adapter.persistence.specification.ProductVariantSearchSpecification;
import com.catalog.application.readmodel.ProductVariantView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class ProductVariantQueryAdapter implements ProductVariantQueryPort {
    private static final Logger log = Loggers.getLogger(ProductVariantQueryAdapter.class);

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
