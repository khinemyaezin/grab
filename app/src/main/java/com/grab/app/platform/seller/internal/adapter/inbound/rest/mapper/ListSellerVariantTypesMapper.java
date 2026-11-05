package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantTypesQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerVariantTypesMapper {

    public abstract ListSellerVariantTypesQuery toQuery(String name);

    public abstract VariantTypeResponse toResponse(VariantTypeOutboundResult result);

    public abstract List<VariantTypeResponse> toResponseList(List<VariantTypeOutboundResult> results);
}

