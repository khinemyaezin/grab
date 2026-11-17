package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import com.grab.service.sellerbff.internal.query.model.SellerProductQueries.ListSellerVariantTypesQuery;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerVariantTypesMapper {

    public abstract ListSellerVariantTypesQuery toQuery(String name);

    public abstract VariantTypeResponse toResponse(VariantTypeOutboundResult result);

    public abstract List<VariantTypeResponse> toResponseList(List<VariantTypeOutboundResult> results);
}

