package com.catalog.application.port.inbound;

import com.catalog.application.query.ProductSearchQuery;
import com.catalog.application.query.ProductSearchResult;
import org.springframework.data.domain.Page;

public interface ProductSearchUseCase {
    Page<ProductSearchResult> execute(ProductSearchQuery query);
}
