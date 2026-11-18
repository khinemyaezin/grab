package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.SearchSellerProductsQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductSearchCriteriaRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchSellerProductsQueryHandler
        implements QueryHandler<SearchSellerProductsQuery, List<ProductSummaryOutboundResult>> {

    private final SellerProductCatalogOutboundPort outboundPort;

    public SearchSellerProductsQueryHandler(SellerProductCatalogOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<ProductSummaryOutboundResult> handle(SearchSellerProductsQuery query) {
        ProductSearchCriteriaRecord record = new ProductSearchCriteriaRecord(
                query.merchantId(),
                query.query(),
                query.variantStatus(),
                query.categoryId(),
                query.productStatus(),
                query.page(),
                query.size()
        );
        return outboundPort.searchProducts(record);
    }

    @Override
    public Class<SearchSellerProductsQuery> getQueryType() {
        return SearchSellerProductsQuery.class;
    }
}

