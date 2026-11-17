package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.catalog.CatalogProductPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.CreateProductRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.CreateVariantRecord;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateCatalogProductMessageMapper {

    public abstract CatalogProductPort.CreateProductMessage toMessage(CreateProductRecord record);

    public abstract CatalogProductPort.CreateVariantMessage toVariantMessage(CreateVariantRecord variant);

    public abstract ProductOutboundResult toResult(CatalogProductPort.ProductResult result);

    public abstract VariantOutboundResult toVariantResult(CatalogProductPort.VariantResult result);
}
