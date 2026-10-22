package com.saleschannel.domain.valueobject;

import com.grab.framework.id.Id;

import java.util.Objects;

public record ChannelRegion(
        Id salesChannelId,
        Id regionId,
        boolean enabled,
        boolean isDefault
) {
    public ChannelRegion {
        Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        Objects.requireNonNull(regionId, "regionId is required");
        if (isDefault && !enabled) {
            throw new IllegalArgumentException("a disabled region cannot be the default");
        }
    }
}
