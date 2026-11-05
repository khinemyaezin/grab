package com.grab.app.channel.saleschannel.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Sales Channel management.
 * Replaces legacy SalesChannelController.
 */
public interface SalesChannelManagementPort {

    ProvisionResult provisionMerchantWebsiteChannel(Id merchantId, String countryCode);

    Optional<ChannelMessageResult> findChannelById(String salesChannelId, String merchantId);

    List<ChannelMessageResult> listChannels(String merchantId, int page, int size);

    record ProvisionResult(
            Id salesChannelId,
            boolean created
    ) {}

    record ChannelMessageResult(
            String salesChannelId,
            String name,
            String type,
            String owner,
            String merchantId,
            String status
    ) {}
}
