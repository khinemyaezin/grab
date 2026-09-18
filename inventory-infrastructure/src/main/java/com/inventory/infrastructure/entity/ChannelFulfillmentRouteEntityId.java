package com.inventory.infrastructure.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
public class ChannelFulfillmentRouteEntityId implements Serializable {
    private Long locationId;
    private String salesChannelId;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChannelFulfillmentRouteEntityId that)) {
            return false;
        }
        return Objects.equals(locationId, that.locationId)
                && Objects.equals(salesChannelId, that.salesChannelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, salesChannelId);
    }
}
