package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CalculatePriceRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CalculatedPriceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceListResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PricePreferenceResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.PriceSetResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.CalculateSellerPricesMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerPriceListMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerPricePreferenceMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerPriceSetMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerPriceListsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.CalculateSellerPricesQuery;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPriceListQuery;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPricePreferenceQuery;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPriceSetQuery;
import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.ListSellerPriceListsQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.CalculatedPriceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerPricingQueryService {

    private final QueryBus queryBus;
    private final GetSellerPriceListMapper getPriceListMapper;
    private final ListSellerPriceListsMapper listPriceListsMapper;
    private final GetSellerPriceSetMapper getPriceSetMapper;
    private final CalculateSellerPricesMapper calculatePricesMapper;
    private final GetSellerPricePreferenceMapper getPricePreferenceMapper;

    public SellerPricingQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerPriceListMapper getPriceListMapper,
            ListSellerPriceListsMapper listPriceListsMapper,
            GetSellerPriceSetMapper getPriceSetMapper,
            CalculateSellerPricesMapper calculatePricesMapper,
            GetSellerPricePreferenceMapper getPricePreferenceMapper
    ) {
        this.queryBus = queryBus;
        this.getPriceListMapper = getPriceListMapper;
        this.listPriceListsMapper = listPriceListsMapper;
        this.getPriceSetMapper = getPriceSetMapper;
        this.calculatePricesMapper = calculatePricesMapper;
        this.getPricePreferenceMapper = getPricePreferenceMapper;
    }

    public Optional<PriceListResponse> getPriceList(String priceListIdStr) {
        GetSellerPriceListQuery query = getPriceListMapper.toQuery(priceListIdStr);
        Optional<PriceListOutboundResult> res = queryBus.dispatch(query);
        return res.map(getPriceListMapper::toResponse);
    }

    public List<PriceListResponse> listPriceLists() {
        ListSellerPriceListsQuery query = listPriceListsMapper.toQuery();
        List<PriceListOutboundResult> results = queryBus.dispatch(query);
        return listPriceListsMapper.toResponse(results);
    }

    public Optional<PriceSetResponse> getPriceSet(String priceSetIdStr) {
        GetSellerPriceSetQuery query = getPriceSetMapper.toQuery(priceSetIdStr);
        Optional<PriceSetOutboundResult> res = queryBus.dispatch(query);
        return res.map(getPriceSetMapper::toResponse);
    }

    public List<CalculatedPriceResponse> calculatePrices(CalculatePriceRequest request) {
        CalculateSellerPricesQuery query = calculatePricesMapper.toQuery(request);
        List<CalculatedPriceOutboundResult> results = queryBus.dispatch(query);
        return calculatePricesMapper.toResponse(results);
    }

    public Optional<PricePreferenceResponse> getPricePreference(String preferenceIdStr) {
        GetSellerPricePreferenceQuery query = getPricePreferenceMapper.toQuery(preferenceIdStr);
        Optional<PricePreferenceOutboundResult> res = queryBus.dispatch(query);
        return res.map(getPricePreferenceMapper::toResponse);
    }
}
