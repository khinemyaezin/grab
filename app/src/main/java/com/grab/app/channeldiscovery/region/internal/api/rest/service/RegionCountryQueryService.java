package com.grab.app.channeldiscovery.region.internal.api.rest.service;

import com.grab.app.channeldiscovery.region.internal.api.rest.dto.response.RegionCountryResponse;
import com.region.application.model.read.ListActiveCountryCodesQuery;
import com.grab.framework.cqrs.query.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionCountryQueryService {
    private final QueryBus queryBus;

    public List<RegionCountryResponse> list() {
        return queryBus.dispatch(new ListActiveCountryCodesQuery()).stream()
                .map(RegionCountryResponse::new)
                .toList();
    }
}
