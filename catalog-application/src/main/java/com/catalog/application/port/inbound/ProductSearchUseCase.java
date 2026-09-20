package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ProductSearchQuery;
import com.catalog.application.model.read.ProductSearchResult;
import org.springframework.data.domain.Page;

public interface ProductSearchUseCase {
    Page<ProductSearchResult> execute(ProductSearchQuery query);
}
