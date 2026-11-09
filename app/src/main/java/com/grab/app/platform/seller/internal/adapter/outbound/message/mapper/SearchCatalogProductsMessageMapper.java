package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.catalog.port.message.CatalogProductPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSearchCriteriaRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchCatalogProductsMessageMapper {

    public abstract CatalogProductPort.ProductSearchCriteria toCriteria(ProductSearchCriteriaRecord record);

    public abstract List<ProductSummaryOutboundResult> toResultList(List<CatalogProductPort.ProductSummaryResult> results);

    public abstract ProductSummaryOutboundResult toResult(CatalogProductPort.ProductSummaryResult result);
}

