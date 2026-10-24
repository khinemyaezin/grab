package com.grab.discovery.application.model.write;

import com.grab.framework.id.Id;

import java.time.Instant;

public record DiscoveryRebuildRun(
        Id runId,
        Instant startedAt,
        RebuildProgressResult.Stage stage,
        Id lastProductId,
        Id lastSalesChannelId,
        long processedCount,
        long invalidatedCount,
        long version
) {
}
