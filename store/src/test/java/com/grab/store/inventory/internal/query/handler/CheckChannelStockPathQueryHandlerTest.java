package com.grab.store.inventory.internal.query.handler;

import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.service.CheckChannelStockPathService;
import com.inventory.application.model.read.CheckChannelStockPathQuery;
import com.inventory.application.model.read.CheckChannelStockPathResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckChannelStockPathServiceTest {

    @Mock
    private InventoryQueryPort inventoryQueryPort;

    private CheckChannelStockPathService handler;

    @BeforeEach
    void setUp() {
        handler = new CheckChannelStockPathService(inventoryQueryPort);
    }

    @Test
    void handle_whenRouteExists() {
        when(inventoryQueryPort.existsActiveRoute("merchant-1", "channel-1")).thenReturn(true);

        CheckChannelStockPathResult result = handler.execute(
                new CheckChannelStockPathQuery("merchant-1", "channel-1")
        );

        assertThat(result.routeExists()).isTrue();
    }

    @Test
    void handle_whenRouteMissing() {
        when(inventoryQueryPort.existsActiveRoute("merchant-1", "channel-1")).thenReturn(false);

        CheckChannelStockPathResult result = handler.execute(
                new CheckChannelStockPathQuery("merchant-1", "channel-1")
        );

        assertThat(result.routeExists()).isFalse();
    }
}
