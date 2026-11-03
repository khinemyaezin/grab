package com.grab.app.merchandising.inventory.internal.adapter.inbound.message;

import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.AllocateStockPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.DeallocateStockPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.GetDiscoveryInventoryFactsPortMapper;
import com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper.GetReorderSuggestionsPortMapper;
import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsResult;
import com.inventory.application.model.read.GetReorderSuggestionResult;
import com.inventory.application.model.read.GetReorderSuggestionsQuery;
import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;
import com.inventory.application.model.write.DeallocateStockCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class InventoryAvailabilityPortAdapter implements InventoryAvailabilityPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final GetDiscoveryInventoryFactsPortMapper discoveryFactsMapper;
    private final AllocateStockPortMapper allocateStockMapper;
    private final DeallocateStockPortMapper deallocateStockMapper;
    private final GetReorderSuggestionsPortMapper reorderSuggestionsMapper;

    public InventoryAvailabilityPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            GetDiscoveryInventoryFactsPortMapper discoveryFactsMapper,
            AllocateStockPortMapper allocateStockMapper,
            DeallocateStockPortMapper deallocateStockMapper,
            GetReorderSuggestionsPortMapper reorderSuggestionsMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.discoveryFactsMapper = discoveryFactsMapper;
        this.allocateStockMapper = allocateStockMapper;
        this.deallocateStockMapper = deallocateStockMapper;
        this.reorderSuggestionsMapper = reorderSuggestionsMapper;
    }

    @Override
    public Optional<DiscoveryStockFactResult> findDiscoveryFacts(Id merchantId, String sku, Id salesChannelId) {
        GetDiscoveryInventoryFactsQuery query = discoveryFactsMapper.toQuery(merchantId, sku, salesChannelId);
        GetDiscoveryInventoryFactsResult result = queryBus.dispatch(query);
        return Optional.ofNullable(discoveryFactsMapper.toResponse(result));
    }

    @Override
    public AllocationMessageResult allocateStock(AllocateStockMessage message) {
        AllocateStockCommand command = allocateStockMapper.toCommand(message);
        AllocateStockResult result = commandBus.dispatch(command);
        return allocateStockMapper.toResponse(result);
    }

    @Override
    public void deallocateStock(DeallocateStockMessage message) {
        DeallocateStockCommand command = deallocateStockMapper.toCommand(message);
        commandBus.dispatch(command);
    }

    @Override
    public List<ReorderSuggestionMessageResult> listReorderSuggestions(Id merchantId, Id locationId, String sku) {
        GetReorderSuggestionsQuery query = reorderSuggestionsMapper.toQuery(merchantId, locationId, sku);
        List<GetReorderSuggestionResult> results = queryBus.dispatch(query);
        if (results == null) {
            return Collections.emptyList();
        }
        return results.stream().map(reorderSuggestionsMapper::toResponse).toList();
    }
}
