package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.api.rest.dto.request.SearchBinRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListBinsRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.SearchBinsRequestMapper;
import com.grab.store.inventory.internal.query.GetBinQuery;
import com.grab.store.inventory.internal.query.GetBinLocationIdQuery;
import com.grab.store.inventory.internal.query.GetBinResult;
import com.grab.store.inventory.internal.query.ListBinsByZoneQuery;
import com.grab.store.inventory.internal.query.ListBinsResult;
import com.grab.store.inventory.internal.query.SearchBinsQuery;
import com.grab.store.inventory.internal.query.SearchBinsResult;
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
