package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.ReorderSuggestionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListReorderSuggestionsMessageMapper {

    public abstract List<ReorderSuggestionOutboundResult> toResultList(List<InventoryAvailabilityPort.ReorderSuggestionMessageResult> results);

    public abstract ReorderSuggestionOutboundResult toResult(InventoryAvailabilityPort.ReorderSuggestionMessageResult result);
}

