package com.grab.discovery.application.model.write;

import com.grab.framework.id.Id;

public record RebuildProgressResult(
        Id runId,
        Stage stage,
        Id lastProductId,
        Id lastSalesChannelId,
        long processedCount,
        long invalidatedCount,
        boolean complete
) {
    public enum Stage {
        PUBLICATIONS,
        EXISTING_PROJECTIONS,
        COMPLETED
    }
}
