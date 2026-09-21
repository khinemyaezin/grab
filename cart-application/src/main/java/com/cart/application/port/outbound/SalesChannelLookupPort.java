package com.cart.application.port.outbound;

import java.util.Optional;

public interface SalesChannelLookupPort {
    Optional<ChannelSnapshot> findEnabled(String salesChannelId);

    record ChannelSnapshot(String salesChannelId, String type) {
    }
}
