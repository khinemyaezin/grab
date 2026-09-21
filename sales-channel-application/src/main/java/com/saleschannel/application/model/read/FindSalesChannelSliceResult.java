package com.saleschannel.application.model.read;

public record FindSalesChannelSliceResult(
        String salesChannelId,
        String type,
        String status,
        String merchantId
) {
    public boolean enabled() {
        return "ENABLED".equals(status);
    }
}
