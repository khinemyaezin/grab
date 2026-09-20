package com.catalog.application.port.outbound;

import com.catalog.application.readmodel.ProductSearchCriteria;
import com.catalog.application.readmodel.ProductVariantView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantQueryPort {
    Page<ProductVariantView> search(ProductSearchCriteria criteria, Pageable pageable);
}
