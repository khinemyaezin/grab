package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
    Page<ProductSummary> search(ProductSearchCriteria criteria, Pageable pageable);
}
