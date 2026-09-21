package com.region.application.model.read;

public record FindRegionSliceResult(
        String regionId,
        String currencyCode,
        String status
) {
}
