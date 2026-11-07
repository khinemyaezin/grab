package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.ListSellerReorderSuggestionsQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerReorderSuggestionsMapper {

    public abstract ListSellerReorderSuggestionsQuery toQuery(String merchantId, String locationId, String sku);

    public abstract List<ReorderSuggestionResponse> toResponse(List<ReorderSuggestionOutboundResult> results);

    public abstract ReorderSuggestionResponse toResponse(ReorderSuggestionOutboundResult result);
}

