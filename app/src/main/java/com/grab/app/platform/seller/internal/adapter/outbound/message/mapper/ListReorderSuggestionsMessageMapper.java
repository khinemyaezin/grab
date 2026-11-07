package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListReorderSuggestionsMessageMapper {

    public abstract List<ReorderSuggestionOutboundResult> toResultList(List<InventoryAvailabilityPort.ReorderSuggestionMessageResult> results);

    public abstract ReorderSuggestionOutboundResult toResult(InventoryAvailabilityPort.ReorderSuggestionMessageResult result);
}

