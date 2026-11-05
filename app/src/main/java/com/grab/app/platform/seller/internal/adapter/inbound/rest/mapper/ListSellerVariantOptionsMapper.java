package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantOptionsQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerVariantOptionsMapper {

    public abstract ListSellerVariantOptionsQuery toQuery(String name, String typeId);

    public abstract VariantOptionResponse toResponse(VariantOptionOutboundResult result);

    public abstract List<VariantOptionResponse> toResponseList(List<VariantOptionOutboundResult> results);
}

