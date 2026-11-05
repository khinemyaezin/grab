package com.grab.app.channel.saleschannel.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Sales Channel Context and Region Resolution.
 * Unifies legacy SalesChannelPort and ChannelRegionQueryPort.
 */
public interface SalesChannelContextPort {

    Optional<SalesChannelSliceResult> findChannelSlice(Id salesChannelId);

    boolean isEnabled(Id salesChannelId);

    List<Id> findRegionsForChannel(Id salesChannelId);

    record SalesChannelSliceResult(
            String salesChannelId,
            String channelType,
            String status,
            String merchantId
    ) {
        public boolean enabled() {
            return "ENABLED".equalsIgnoreCase(status);
        }

        public boolean website() {
            return "WEBSITE".equalsIgnoreCase(channelType);
        }

        public boolean marketplace() {
            return "MARKETPLACE".equalsIgnoreCase(channelType);
        }
    }
}
