package com.saleschannel.application.model.read;

public record CheckSalesChannelUsableResult(
        boolean found,
        boolean enabled,
        boolean ownedByMerchant
) {
    public boolean usable() {
        return found && enabled && ownedByMerchant;
    }

    public static CheckSalesChannelUsableResult missing() {
        return new CheckSalesChannelUsableResult(false, false, false);
    }
}
