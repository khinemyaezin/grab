package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.inventory.internal.query.CheckChannelStockPathQuery;
import com.grab.store.inventory.internal.query.CheckChannelStockPathResult;
import com.inventory.domain.repository.ChannelFulfillmentRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckChannelStockPathQueryHandlerTest {

    @Mock
    private ChannelFulfillmentRouteRepository channelFulfillmentRouteRepository;

    private CheckChannelStockPathQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CheckChannelStockPathQueryHandler(channelFulfillmentRouteRepository, new IdGenerator() {
            @Override
            public Id generateId() {
                return new CommonId("new");
            }

            @Override
            public Id convertIdFrom(String id) {
                return new CommonId(id);
            }
        });
    }

    @Test
    void handle_whenRouteExists() {
        when(channelFulfillmentRouteRepository.existsActiveForMerchantAndChannel(
                new CommonId("merchant-1"),
                new CommonId("channel-1")
        )).thenReturn(true);

        CheckChannelStockPathResult result = handler.handle(
                new CheckChannelStockPathQuery("merchant-1", "channel-1")
        );

        assertThat(result.routeExists()).isTrue();
    }

    @Test
    void handle_whenRouteMissing() {
        when(channelFulfillmentRouteRepository.existsActiveForMerchantAndChannel(
                new CommonId("merchant-1"),
                new CommonId("channel-1")
        )).thenReturn(false);

        CheckChannelStockPathResult result = handler.handle(
                new CheckChannelStockPathQuery("merchant-1", "channel-1")
        );

        assertThat(result.routeExists()).isFalse();
    }
}
