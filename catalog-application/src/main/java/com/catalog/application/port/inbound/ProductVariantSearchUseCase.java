package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ProductVariantSummaryQuery;
import com.catalog.application.model.read.ProductVariantSummaryResult;
import org.springframework.data.domain.Page;

public interface ProductVariantSearchUseCase {
    Page<ProductVariantSummaryResult> execute(ProductVariantSummaryQuery query);
}
