package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.LocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.SearchLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.GetLocationByCodeRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.GetLocationRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.ListLocationsRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.SearchLocationsRequestMapper;
import com.inventory.application.model.read.GetLocationByCodeQuery;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.model.read.ListLocationsQuery;
import com.inventory.application.model.read.ListLocationsResult;
import com.inventory.application.model.read.SearchLocationsQuery;
import com.inventory.application.model.read.SearchLocationsResult;
import com.inventory.domain.enums.LocationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationQueryService {

    private final QueryBus queryBus;
    private final GetLocationRequestMapper getLocationRequestMapper;
    private final GetLocationByCodeRequestMapper getLocationByCodeRequestMapper;
    private final ListLocationsRequestMapper listLocationsRequestMapper;
    private final SearchLocationsRequestMapper searchLocationsRequestMapper;

    public LocationResponse getLocation(String locationId) {
        GetLocationQuery query = getLocationRequestMapper.toQuery(locationId);
        GetLocationResult result = queryBus.dispatch(query);
        return getLocationRequestMapper.toResponse(result);
    }

    public LocationResponse getLocationByCode(String code) {
        GetLocationByCodeQuery query = getLocationByCodeRequestMapper.toQuery(code);
        GetLocationResult result = queryBus.dispatch(query);
        return getLocationByCodeRequestMapper.toResponse(result);
    }

    public Page<LocationResponse> listLocations(String merchantId, Boolean active, LocationType type, Pageable pageable) {
        ListLocationsQuery query = listLocationsRequestMapper.toQuery(merchantId, active, type, pageable);
        Page<ListLocationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listLocationsRequestMapper::toResponse);
    }

    public Page<LocationResponse> searchLocations(String merchantId, SearchLocationRequest request, Pageable pageable) {
        SearchLocationsQuery query = searchLocationsRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchLocationsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchLocationsRequestMapper::toResponse);
    }
}
