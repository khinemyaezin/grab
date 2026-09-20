package com.catalog.application.port.outbound;

import com.catalog.application.model.read.ProductSearchCriteria;
import com.catalog.application.model.read.ProductVariantView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantQueryPort {
    Page<ProductVariantView> search(ProductSearchCriteria criteria, Pageable pageable);
}
