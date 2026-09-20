package com.catalog.adapter.persistence.adapter;

import com.catalog.application.port.outbound.ProductQueryPort;
import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.adapter.persistence.specification.ProductSearchSpecification;
import com.catalog.application.readmodel.ProductHeroMediaView;
import com.catalog.application.readmodel.ProductPublicationView;
import com.catalog.application.readmodel.ProductVariantRefView;
import com.catalog.application.readmodel.ProductView;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.support.PersistenceExecutor;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private static final Logger log = Loggers.getLogger(ProductQueryAdapter.class);

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

    @Override
    public List<ProductHeroMediaView> findHeroMediasByProductIds(Collection<String> productIds) {
        log.debug("Finding hero medias for {} products", productIds == null ? 0 : productIds.size());
        return executor.query("Product", () ->
                productSearchSpecification.findHeroMediasByProductIds(productIds));
    }

    @Override
    public List<ProductVariantRefView> findActiveVariantsByProductIds(Collection<String> productIds) {
        return executor.query("Product", () ->
                productSearchSpecification.findActiveVariantsByProductIds(productIds));
    }

    @Override
    public List<ProductPublicationView> findPublicationsByProductIds(Collection<String> productIds) {
        return executor.query("Product", () ->
                productSearchSpecification.findPublicationsByProductIds(productIds));
    }
}
