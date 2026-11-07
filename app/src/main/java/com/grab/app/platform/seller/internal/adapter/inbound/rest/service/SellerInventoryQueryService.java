package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerBinMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerInventoryItemMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerLocationMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerStockMovementMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerZoneMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerReorderSuggestionsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerBinQuery;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerInventoryItemQuery;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerLocationQuery;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerStockMovementQuery;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerZoneQuery;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.ListSellerReorderSuggestionsQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerInventoryQueryService {

    private final QueryBus queryBus;
    private final GetSellerInventoryItemMapper getItemMapper;
    private final GetSellerStockMovementMapper getMovementMapper;
    private final GetSellerLocationMapper getLocationMapper;
    private final GetSellerZoneMapper getZoneMapper;
    private final GetSellerBinMapper getBinMapper;
    private final ListSellerReorderSuggestionsMapper listReorderSuggestionsMapper;

    public SellerInventoryQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerInventoryItemMapper getItemMapper,
            GetSellerStockMovementMapper getMovementMapper,
            GetSellerLocationMapper getLocationMapper,
            GetSellerZoneMapper getZoneMapper,
            GetSellerBinMapper getBinMapper,
            ListSellerReorderSuggestionsMapper listReorderSuggestionsMapper
    ) {
        this.queryBus = queryBus;
        this.getItemMapper = getItemMapper;
        this.getMovementMapper = getMovementMapper;
        this.getLocationMapper = getLocationMapper;
        this.getZoneMapper = getZoneMapper;
        this.getBinMapper = getBinMapper;
        this.listReorderSuggestionsMapper = listReorderSuggestionsMapper;
    }

    public Optional<InventoryItemResponse> getItem(String itemIdStr) {
        GetSellerInventoryItemQuery query = getItemMapper.toQuery(itemIdStr);
        Optional<InventoryItemOutboundResult> res = queryBus.dispatch(query);
        return res.map(getItemMapper::toResponse);
    }

    public Optional<StockMovementResponse> getLatestMovement(String itemIdStr) {
        GetSellerStockMovementQuery query = getMovementMapper.toQuery(itemIdStr);
        Optional<StockMovementOutboundResult> res = queryBus.dispatch(query);
        return res.map(getMovementMapper::toResponse);
    }

    public Optional<FacilityLocationResponse> getLocation(String locationIdStr) {
        GetSellerLocationQuery query = getLocationMapper.toQuery(locationIdStr);
        Optional<FacilityLocationOutboundResult> res = queryBus.dispatch(query);
        return res.map(getLocationMapper::toResponse);
    }

    public Optional<FacilityZoneResponse> getZone(String zoneIdStr) {
        GetSellerZoneQuery query = getZoneMapper.toQuery(zoneIdStr);
        Optional<FacilityZoneOutboundResult> res = queryBus.dispatch(query);
        return res.map(getZoneMapper::toResponse);
    }

    public Optional<FacilityBinResponse> getBin(String binIdStr) {
        GetSellerBinQuery query = getBinMapper.toQuery(binIdStr);
        Optional<FacilityBinOutboundResult> res = queryBus.dispatch(query);
        return res.map(getBinMapper::toResponse);
    }

    public List<ReorderSuggestionResponse> listReorderSuggestions(String merchantIdStr, String locationIdStr, String sku) {
        ListSellerReorderSuggestionsQuery query = listReorderSuggestionsMapper.toQuery(merchantIdStr, locationIdStr, sku);
        List<ReorderSuggestionOutboundResult> results = queryBus.dispatch(query);
        return listReorderSuggestionsMapper.toResponse(results);
    }
}
