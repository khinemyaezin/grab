package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.product.port.message.CatalogVariantDefinitionPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListCatalogVariantTypesMessageMapper {

    public abstract List<VariantTypeOutboundResult> toResultList(List<CatalogVariantDefinitionPort.VariantTypeMessageResult> results);

    public abstract VariantTypeOutboundResult toResult(CatalogVariantDefinitionPort.VariantTypeMessageResult result);
}

