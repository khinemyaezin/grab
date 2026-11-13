package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionCountryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.RegionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerRegionMapper;
import com.grab.app.platform.seller.internal.query.model.SellerRegionQueries.GetSellerRegionQuery;
import com.grab.app.platform.seller.internal.query.model.SellerRegionQueries.ListActiveRegionCountriesQuery;
import com.grab.app.platform.seller.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerRegionQueryService {

    private final QueryBus queryBus;
    private final GetSellerRegionMapper mapper;

    public SellerRegionQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerRegionMapper mapper
    ) {
        this.queryBus = queryBus;
        this.mapper = mapper;
    }

    public Optional<RegionResponse> get(String regionId) {
        GetSellerRegionQuery query = mapper.toQuery(regionId);
        Optional<RegionOutboundResult> result = queryBus.dispatch(query);
        return result.map(mapper::toResponse);
    }

    public List<RegionCountryResponse> countries() {
        List<String> codes = queryBus.dispatch(new ListActiveRegionCountriesQuery());
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .map(RegionCountryResponse::new)
                .toList();
    }
}

