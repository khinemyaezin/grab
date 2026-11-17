package com.grab.service.merchandising.inventory.internal.adapter.inbound.message;

import com.grab.framework.id.impl.CommonId;
import com.grab.service.merchandising.TestSupport;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.AllocateStockPortMapper;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.DeallocateStockPortMapper;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.GetDiscoveryInventoryFactsPortMapper;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.GetReorderSuggestionsPortMapper;
import com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort.AllocateStockMessage;
import com.inventory.application.model.read.GetDiscoveryInventoryFactsQuery;
import com.inventory.application.model.write.AllocateStockCommand;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryAvailabilityPortAdapterTest {

    @Test
    void dispatchesInventoryReadAndWriteOperationsOnMerchandisingBuses() {
        TestSupport.RecordingCommandBus commandBus = new TestSupport.RecordingCommandBus();
        TestSupport.RecordingQueryBus queryBus = new TestSupport.RecordingQueryBus();
        InventoryAvailabilityPortAdapter adapter = new InventoryAvailabilityPortAdapter(
                commandBus,
                queryBus,
                TestSupport.mapper(GetDiscoveryInventoryFactsPortMapper.class),
                TestSupport.mapper(AllocateStockPortMapper.class),
                TestSupport.mapper(DeallocateStockPortMapper.class),
                TestSupport.mapper(GetReorderSuggestionsPortMapper.class));

        var merchantId = new CommonId("merchant-1");
        var channelId = new CommonId("channel-1");
        assertThat(adapter.findDiscoveryFacts(merchantId, "SKU-1", channelId)).isNull();
        assertThat(queryBus.dispatchedQuery()).isInstanceOf(GetDiscoveryInventoryFactsQuery.class);

        var message = new AllocateStockMessage("SKU-1", 2, "order-1", "line-1", null, null,
                null, "order", "order-1");
        adapter.allocateStock(message);
        assertThat(commandBus.dispatchedCommand()).isInstanceOf(AllocateStockCommand.class);
    }
}
