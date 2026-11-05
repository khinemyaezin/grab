package com.grab.app.platform.seller.port.outbound;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Sales Channels and Publishable Keys.
 */
public interface SellerSalesChannelOutboundPort {

    ProvisionOutboundResult provisionMerchantWebsiteChannel(Id merchantId, String countryCode);

    Optional<ChannelOutboundResult> findChannelById(String salesChannelId, String merchantId);

    List<ChannelOutboundResult> listChannels(String merchantId, int page, int size);

    PublishableKeyOutboundResult createPublishableKey(CreatePublishableKeyRecord record);

    void revokePublishableKey(Id salesChannelId, Id keyId);

    Optional<KeyResolutionOutboundResult> resolveKey(String rawToken);

    record CreatePublishableKeyRecord(
            Id salesChannelId,
            String label
    ) {}

    record ChannelOutboundResult(
            String salesChannelId,
            String name,
            String type,
            String owner,
            String merchantId,
            String status
    ) {}

    record ProvisionOutboundResult(
            String salesChannelId,
            boolean created
    ) {}

    record PublishableKeyOutboundResult(
            String keyId,
            String salesChannelId,
            String label,
            String rawToken,
            String displayPrefix,
            Instant createdAt
    ) {}

    record KeyResolutionOutboundResult(
            String salesChannelId,
            boolean channelEnabled,
            boolean keyActive
    ) {}
}

