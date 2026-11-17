package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.ListSellerReorderSuggestionsQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class ListSellerReorderSuggestionsMapper {

    public abstract ListSellerReorderSuggestionsQuery toQuery(String merchantId, String locationId, String sku);

    public abstract List<ReorderSuggestionResponse> toResponse(List<ReorderSuggestionOutboundResult> results);

    public abstract ReorderSuggestionResponse toResponse(ReorderSuggestionOutboundResult result);
}

