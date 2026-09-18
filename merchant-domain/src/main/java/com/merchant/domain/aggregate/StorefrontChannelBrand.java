package com.merchant.domain.aggregate;

import com.grab.framework.domain.AggregateRoot;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class StorefrontChannelBrand extends AggregateRoot<Id> {
    private final Id storefrontId;
    private final Id salesChannelId;

    private StorefrontChannelBrand(Id id, Id storefrontId, Id salesChannelId) {
        super(id);
        this.storefrontId = Objects.requireNonNull(storefrontId, "storefrontId is required");
        this.salesChannelId = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
    }

    public static StorefrontChannelBrand attach(Id storefrontId, Id salesChannelId) {
        return restore(storefrontId, salesChannelId);
    }

    public static StorefrontChannelBrand restore(Id storefrontId, Id salesChannelId) {
        Id storefront = Objects.requireNonNull(storefrontId, "storefrontId is required");
        Id channel = Objects.requireNonNull(salesChannelId, "salesChannelId is required");
        return new StorefrontChannelBrand(identity(storefront, channel), storefront, channel);
    }

    public void detach() {
        // Presence-only association; deleting the row is the detach.
    }

    public static Id identity(Id storefrontId, Id salesChannelId) {
        return new CommonId(storefrontId.getValue() + ":" + salesChannelId.getValue());
    }
}
