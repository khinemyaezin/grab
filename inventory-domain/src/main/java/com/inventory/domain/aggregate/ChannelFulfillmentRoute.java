package com.inventory.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.event.LocationLinkedToChannelEvent;
import com.inventory.domain.event.LocationUnlinkedFromChannelEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class ChannelFulfillmentRoute extends AggregateRoot<Id> {
    private final Id locationId;
    private final Id salesChannelId;

    private ChannelFulfillmentRoute(Id id, Id locationId, Id salesChannelId) {
        super(id);
        this.locationId = Objects.requireNonNull(locationId, "locationId is required");
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
    }

    public static ChannelFulfillmentRoute link(Id locationId, Id salesChannelId) {
        ChannelFulfillmentRoute route = restore(locationId, salesChannelId);
        route.addEvent(new LocationLinkedToChannelEvent(locationId, salesChannelId, LocalDateTime.now()));
        return route;
    }

    public static ChannelFulfillmentRoute restore(Id locationId, Id salesChannelId) {
        Id location = Objects.requireNonNull(locationId, "locationId is required");
        Id channel = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        return new ChannelFulfillmentRoute(identity(location, channel), location, channel);
    }

    public void unlink() {
        addEvent(new LocationUnlinkedFromChannelEvent(locationId, salesChannelId, LocalDateTime.now()));
    }

    public static Id identity(Id locationId, Id salesChannelId) {
        return new CommonId(locationId.getValue() + ":" + salesChannelId.getValue());
    }
}
