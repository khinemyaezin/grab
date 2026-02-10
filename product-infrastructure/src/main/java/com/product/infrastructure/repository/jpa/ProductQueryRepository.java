package com.product.infrastructure.repository.jpa;

import com.product.infrastructure.specification.jpa.ProductSearchCriteria;
import com.product.infrastructure.specification.jpa.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
    Page<ProductSummary> search(ProductSearchCriteria criteria, Pageable pageable);
}
