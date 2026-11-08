package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.catalog.port.message.CatalogProductPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.CreateProductRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.CreateVariantRecord;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateCatalogProductMessageMapper {

    public abstract CatalogProductPort.CreateProductMessage toMessage(CreateProductRecord record);

    public abstract CatalogProductPort.CreateVariantMessage toVariantMessage(CreateVariantRecord variant);

    public abstract ProductOutboundResult toResult(CatalogProductPort.ProductResult result);

    public abstract VariantOutboundResult toVariantResult(CatalogProductPort.VariantResult result);
}
