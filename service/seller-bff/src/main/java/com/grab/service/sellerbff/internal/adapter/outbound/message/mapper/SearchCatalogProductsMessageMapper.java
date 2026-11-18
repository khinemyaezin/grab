package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.catalog.CatalogProductPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductSearchCriteriaRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class SearchCatalogProductsMessageMapper {

    public abstract CatalogProductPort.ProductSearchCriteria toCriteria(ProductSearchCriteriaRecord record);

    public abstract List<ProductSummaryOutboundResult> toResultList(List<CatalogProductPort.ProductSummaryResult> results);

    public abstract ProductSummaryOutboundResult toResult(CatalogProductPort.ProductSummaryResult result);
}

