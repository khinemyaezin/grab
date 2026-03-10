package com.catalog.infrastructure.repository.jpa.impl;

import com.catalog.infrastructure.mapper.jpa.ProductSummaryMapper;
import com.catalog.infrastructure.repository.jpa.ProductQueryRepository;
import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductSummary;
import com.catalog.infrastructure.specification.jpa.ProductSummaryJpqlQuery;
import com.grab.framework.support.PersistenceExecutor;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class ProductQueryJpqlRepository implements ProductQueryRepository {
    private final EntityManager entityManager;
    private final ProductSummaryMapper productSummaryMapper;
    private final PersistenceExecutor executor;

    @Override
    public Page<ProductSummary> search(ProductSearchCriteria criteria, Pageable pageable) {
        return executor.query("Product", () -> ProductSummaryJpqlQuery.search(entityManager, criteria, pageable)
                .map(productSummaryMapper::toProductSummary));
    }
}
