package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductVariantView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantQueryRepository {
    Page<ProductVariantView> search(ProductSearchCriteria criteria, Pageable pageable);
}
