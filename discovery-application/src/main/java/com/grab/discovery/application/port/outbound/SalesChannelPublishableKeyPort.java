package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.Optional;

public interface SalesChannelPublishableKeyPort {
    Optional<ResolvedChannel> resolve(String rawToken);

    record ResolvedChannel(Id salesChannelId, boolean enabled) {
    }
}
