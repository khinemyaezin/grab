package com.inventory.application.port.outbound;

import com.inventory.domain.event.InventoryItemStatusChangedEvent;
import com.inventory.domain.event.LocationActivatedEvent;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.event.LocationDeletedEvent;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationUnlinkedFromChannelEvent;
import com.inventory.domain.event.StockAdjustedEvent;
import com.inventory.domain.event.StockReceivedEvent;
import com.inventory.domain.event.StockReservedEvent;
import com.inventory.domain.event.StockShippedEvent;

public interface InventoryIntegrationEventPublisher {

    void publishStockReceived(StockReceivedEvent event);

    void publishStockReserved(StockReservedEvent event);

    void publishStockAdjusted(StockAdjustedEvent event);

    void publishStockShipped(StockShippedEvent event);

    void publishLocationLinked(LocationLinkedToChannelEvent event);

    void publishLocationUnlinked(LocationUnlinkedFromChannelEvent event);

    void publishInventoryStatusChanged(InventoryItemStatusChangedEvent event);

    void publishLocationActivated(LocationActivatedEvent event);

    void publishLocationDeactivated(LocationDeactivatedEvent event);

    void publishLocationDeleted(LocationDeletedEvent event);
}

