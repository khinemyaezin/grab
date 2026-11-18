package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.ListSellerVariantOptionsQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerVariantOptionsMapper {

    public abstract ListSellerVariantOptionsQuery toQuery(String name, String typeId);

    public abstract VariantOptionResponse toResponse(VariantOptionOutboundResult result);

    public abstract List<VariantOptionResponse> toResponseList(List<VariantOptionOutboundResult> results);
}

