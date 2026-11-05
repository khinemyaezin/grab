package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.ListSellerPriceListsQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerPriceListsMapper {

    public ListSellerPriceListsQuery toQuery() {
        return new ListSellerPriceListsQuery();
    }

    public abstract List<PriceListResponse> toResponse(List<PriceListOutboundResult> results);

    public abstract PriceListResponse toResponse(PriceListOutboundResult result);
}

