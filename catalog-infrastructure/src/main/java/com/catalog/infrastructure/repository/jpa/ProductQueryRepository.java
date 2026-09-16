package com.catalog.infrastructure.repository.jpa;

import com.catalog.infrastructure.specification.jpa.ProductSearchCriteria;
import com.catalog.infrastructure.view.ProductHeroMediaView;
import com.catalog.infrastructure.view.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface ProductQueryRepository {
    Page<ProductView> search(ProductSearchCriteria criteria, Pageable pageable);

    List<ProductHeroMediaView> findHeroMediasByProductIds(Collection<String> productIds);
}
