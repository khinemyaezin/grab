package com.grab.app.channel.saleschannel.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Sales Channel management.
 * Replaces legacy SalesChannelController.
 */
public interface SalesChannelManagementPort {

    SalesChannelResult createSalesChannel(CreateSalesChannelMessage message);

    SalesChannelResult updateSalesChannel(Id channelId, UpdateSalesChannelMessage message);

    void enableSalesChannel(Id channelId);

    void disableSalesChannel(Id channelId);

    Optional<SalesChannelResult> findChannelById(Id channelId);

    List<SalesChannelResult> listChannels(Id merchantId);

    record CreateSalesChannelMessage(
            Id merchantId,
            String name,
            String description,
            String channelType // WEBSITE, MARKETPLACE, MOBILE_APP
    ) {}

    record UpdateSalesChannelMessage(
            String name,
            String description
    ) {}

    record SalesChannelResult(
            Id id,
            Id merchantId,
            String name,
            String description,
            String channelType,
            String status,
            Instant createdAt
    ) {}
}

