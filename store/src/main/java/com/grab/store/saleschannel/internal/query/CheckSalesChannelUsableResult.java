package com.grab.store.saleschannel.internal.query;

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
