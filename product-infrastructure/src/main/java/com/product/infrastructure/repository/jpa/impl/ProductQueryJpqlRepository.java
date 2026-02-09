package com.product.infrastructure.repository.jpa.impl;

import com.product.infrastructure.mapper.jpa.ProductSummaryMapper;
import com.product.infrastructure.repository.jpa.ProductQueryRepository;
import com.product.infrastructure.specification.jpa.ProductSearchCriteria;
import com.product.infrastructure.specification.jpa.ProductSummary;
import com.product.infrastructure.specification.jpa.ProductSummaryJpqlQuery;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
public class ProductQueryJpqlRepository implements ProductQueryRepository {
    private final EntityManager entityManager;
    private final ProductSummaryMapper productSummaryMapper;

    @Override
    public Page<ProductSummary> search(ProductSearchCriteria criteria, Pageable pageable) {
        return ProductSummaryJpqlQuery.search(entityManager, criteria, pageable)
                .map(productSummaryMapper::toProductSummary);
    }
}