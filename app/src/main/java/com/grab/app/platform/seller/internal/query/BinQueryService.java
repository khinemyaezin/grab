package com.grab.app.platform.seller.internal.query;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.SearchBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.BinResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.GetBinRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.ListBinsRequestMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper.SearchBinsRequestMapper;
import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinLocationIdQuery;
import com.inventory.application.model.read.GetBinResult;
import com.inventory.application.model.read.ListBinsByZoneQuery;
import com.inventory.application.model.read.ListBinsResult;
import com.inventory.application.model.read.SearchBinsQuery;
import com.inventory.application.model.read.SearchBinsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BinQueryService {

    private final QueryBus queryBus;
    private final GetBinRequestMapper getBinRequestMapper;
    private final ListBinsRequestMapper listBinsRequestMapper;
    private final SearchBinsRequestMapper searchBinsRequestMapper;
    private final IdGenerator idGenerator;

    public BinResponse getBin(String binId) {
        GetBinQuery query = getBinRequestMapper.toQuery(binId);
        GetBinResult result = queryBus.dispatch(query);
        return getBinRequestMapper.toResponse(result);
    }

    public Page<BinResponse> listBins(String zoneId, Boolean active, Pageable pageable) {
        ListBinsByZoneQuery query = listBinsRequestMapper.toQuery(zoneId, active, pageable);
        Page<ListBinsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listBinsRequestMapper::toResponse);
    }

    public Page<BinResponse> searchBins(String merchantId, SearchBinRequest request, Pageable pageable) {
        SearchBinsQuery query = searchBinsRequestMapper.toQuery(merchantId, request, pageable);
        Page<SearchBinsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(searchBinsRequestMapper::toResponse);
    }

    public String getLocationId(String binId) {
        GetBinLocationIdQuery query = new GetBinLocationIdQuery(idGenerator.convertIdFrom(binId));
        return queryBus.dispatch(query);
    }
}
