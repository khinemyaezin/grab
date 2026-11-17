package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.catalog.CatalogVariantDefinitionPort;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListCatalogVariantOptionsMessageMapper {

    public abstract List<VariantOptionOutboundResult> toResultList(List<CatalogVariantDefinitionPort.VariantOptionMessageResult> results);

    public abstract VariantOptionOutboundResult toResult(CatalogVariantDefinitionPort.VariantOptionMessageResult result);
}

