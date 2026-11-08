package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.catalog.port.message.CatalogVariantDefinitionPort;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListCatalogVariantOptionsMessageMapper {

    public abstract List<VariantOptionOutboundResult> toResultList(List<CatalogVariantDefinitionPort.VariantOptionMessageResult> results);

    public abstract VariantOptionOutboundResult toResult(CatalogVariantDefinitionPort.VariantOptionMessageResult result);
}

