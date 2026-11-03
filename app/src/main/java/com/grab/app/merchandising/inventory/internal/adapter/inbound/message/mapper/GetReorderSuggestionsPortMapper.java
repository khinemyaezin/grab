package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort.ReorderSuggestionMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetReorderSuggestionsPortMapper {

    public GetReorderSuggestionsQuery toQuery(Id merchantId, Id locationId, String sku) {
        return new GetReorderSuggestionsQuery(merchantId, locationId, sku);
    }

    public abstract ReorderSuggestionMessageResult toResponse(GetReorderSuggestionResult result);
}

