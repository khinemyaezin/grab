package com.saleschannel.infrastructure.view;

import com.saleschannel.domain.enums.ChannelOwner;
import com.saleschannel.domain.enums.ChannelStatus;
import com.saleschannel.domain.enums.ChannelType;

import java.time.Instant;

public record SalesChannelView(
        String id,
        String name,
        ChannelType type,
        ChannelOwner owner,
        String merchantId,
        ChannelStatus status,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
