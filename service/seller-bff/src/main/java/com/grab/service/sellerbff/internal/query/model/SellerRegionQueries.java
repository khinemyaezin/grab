package com.grab.service.sellerbff.internal.query.model;

import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.framework.cqrs.query.Query;

import java.util.List;
import java.util.Optional;

public final class SellerRegionQueries {

    private SellerRegionQueries() {}

    public record GetSellerRegionQuery(
            String regionId
    ) implements Query<Optional<RegionOutboundResult>> {}

    public record ListActiveRegionCountriesQuery() implements Query<List<String>> {}
}

