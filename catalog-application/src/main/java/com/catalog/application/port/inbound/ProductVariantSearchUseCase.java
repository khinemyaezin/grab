package com.catalog.application.port.inbound;

import com.catalog.application.query.ProductVariantSummaryQuery;
import com.catalog.application.query.ProductVariantSummaryResult;
import org.springframework.data.domain.Page;

public interface ProductVariantSearchUseCase {
    Page<ProductVariantSummaryResult> execute(ProductVariantSummaryQuery query);
}
