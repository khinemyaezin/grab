package com.grab.app.merchandising.product.internal.adapter.inbound.message.mapper;

import com.catalog.application.model.read.ProductSearchQuery;
import com.catalog.application.model.read.ProductSearchResult;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductSearchCriteria;
import com.grab.app.merchandising.product.port.message.CatalogProductPort.ProductSummaryResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageRequest;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchProductsPortMapper {

    public ProductSearchQuery toQuery(ProductSearchCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        String merchantIdStr = criteria.merchantId() != null ? criteria.merchantId().getValue() : null;
        String categoryIdStr = criteria.categoryId() != null ? criteria.categoryId().getValue() : null;
        return new ProductSearchQuery(
                merchantIdStr,
                criteria.query(),
                criteria.variantStatus(),
                categoryIdStr,
                criteria.productStatus(),
                PageRequest.of(criteria.page(), criteria.size())
        );
    }

    public abstract ProductSummaryResult toResponse(ProductSearchResult result);
}

