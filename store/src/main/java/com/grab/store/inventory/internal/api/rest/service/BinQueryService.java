package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.api.rest.dto.response.BinResponse;
import com.grab.store.inventory.internal.api.rest.mapper.GetBinRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ListBinsRequestMapper;
import com.grab.store.inventory.internal.query.GetBinQuery;
import com.grab.store.inventory.internal.query.GetBinLocationIdQuery;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.query.GetBinResult;
import com.grab.store.inventory.internal.query.ListBinsResult;
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
    private final IdGenerator idGenerator;

    public BinResponse getBin(String binId) {
        GetBinQuery query = getBinRequestMapper.toQuery(binId);
        GetBinResult result = queryBus.dispatch(query);
        return getBinRequestMapper.toResponse(result);
    }

    public Page<BinResponse> listBins(String zoneId, Boolean active, Pageable pageable) {
        var query = listBinsRequestMapper.toQuery(zoneId, active, pageable);
        Page<ListBinsResult> resultPage = queryBus.dispatch(query);
        return resultPage.map(listBinsRequestMapper::toResponse);
    }

    public String getLocationId(String binId) {
        var query = new GetBinLocationIdQuery(idGenerator.convertIdFrom(binId));
        return queryBus.dispatch(query);
    }
}
