package com.grab.store.saleschannel.port;

import java.util.Optional;

public interface SalesChannelQuery {
    Optional<SalesChannelSlice> find(String salesChannelId);

    boolean isEnabled(String salesChannelId);

    record SalesChannelSlice(
            String salesChannelId,
            String type,
            String status,
            String merchantId
    ) {
        public boolean enabled() {
            return "ENABLED".equals(status);
        }

        public boolean website() {
            return "WEBSITE".equals(type);
        }

        public boolean marketplace() {
            return "MARKETPLACE".equals(type);
        }
    }
}
