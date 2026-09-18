package com.inventory.domain.aggregate;

import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationUnlinkedFromChannelEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelFulfillmentRouteTest {

    @Test
    void link_emitsLinkedEventAndUsesNaturalKey() {
        var locationId = new CommonId("loc-1");
        var channelId = new CommonId("channel-1");

        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.link(locationId, channelId);

        assertThat(route.getId().getValue()).isEqualTo("loc-1:channel-1");
        assertThat(route.getLocationId()).isEqualTo(locationId);
        assertThat(route.getSalesChannelId()).isEqualTo(channelId);
        assertThat(route.getEvents()).hasSize(1);
        assertThat(route.getEvents().getFirst()).isInstanceOf(LocationLinkedToChannelEvent.class);
    }

    @Test
    void restore_doesNotEmitEvents() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(
                new CommonId("loc-1"),
                new CommonId("channel-1")
        );

        assertThat(route.getEvents()).isEmpty();
    }

    @Test
    void unlink_emitsUnlinkedEvent() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(
                new CommonId("loc-1"),
                new CommonId("channel-1")
        );

        route.unlink();

        assertThat(route.getEvents()).hasSize(1);
        assertThat(route.getEvents().getFirst()).isInstanceOf(LocationUnlinkedFromChannelEvent.class);
    }
}
