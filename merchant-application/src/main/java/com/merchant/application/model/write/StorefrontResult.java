package com.merchant.application.model.write;

import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.enums.StorefrontStatus;
import com.merchant.application.model.read.StorefrontView;

import java.time.Instant;

public record StorefrontResult(
        String storefrontId,
        String merchantId,
        String name,
        String slug,
        String status,
        String lifecycleReason,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static StorefrontResult from(Storefront storefront) {
        return new StorefrontResult(
                storefront.getId().getValue(),
                storefront.getMerchantId().getValue(),
                storefront.getName().value(),
                storefront.getSlug().value(),
                storefront.getStatus().name(),
                storefront.getLifecycleReason() == null ? null : storefront.getLifecycleReason().value(),
                storefront.getCreatedAt(),
                storefront.getUpdatedAt(),
                storefront.getVersion()
        );
    }

    public static StorefrontResult from(StorefrontView view) {
        StorefrontStatus status = view.status();
        return new StorefrontResult(
                view.id(),
                view.merchantId(),
                view.name(),
                view.slug(),
                status == null ? null : status.name(),
                view.lifecycleReason(),
                view.createdAt(),
                view.updatedAt(),
                view.version()
        );
    }
}
