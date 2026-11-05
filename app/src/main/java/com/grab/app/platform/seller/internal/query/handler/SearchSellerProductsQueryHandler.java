package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.SearchSellerProductsQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSearchCriteriaRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
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

