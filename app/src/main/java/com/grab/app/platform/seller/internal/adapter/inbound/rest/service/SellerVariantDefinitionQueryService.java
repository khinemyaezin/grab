package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantOptionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantTypeResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerVariantOptionsMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.ListSellerVariantTypesMapper;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantOptionsQuery;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.ListSellerVariantTypesQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOptionOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantTypeOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerVariantDefinitionQueryService {

    private final QueryBus queryBus;
    private final ListSellerVariantTypesMapper listTypesMapper;
    private final ListSellerVariantOptionsMapper listOptionsMapper;

    public SellerVariantDefinitionQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            ListSellerVariantTypesMapper listTypesMapper,
            ListSellerVariantOptionsMapper listOptionsMapper) {
        this.queryBus = queryBus;
        this.listTypesMapper = listTypesMapper;
        this.listOptionsMapper = listOptionsMapper;
    }

    public List<VariantTypeResponse> listTypes(String name) {
        ListSellerVariantTypesQuery query = listTypesMapper.toQuery(name);
        List<VariantTypeOutboundResult> results = queryBus.dispatch(query);
        return results != null ? listTypesMapper.toResponseList(results) : List.of();
    }

    public List<VariantOptionResponse> listOptions(String name, String typeIdStr) {
        ListSellerVariantOptionsQuery query = listOptionsMapper.toQuery(name, typeIdStr);
        List<VariantOptionOutboundResult> results = queryBus.dispatch(query);
        return results != null ? listOptionsMapper.toResponseList(results) : List.of();
    }
}
