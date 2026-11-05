package com.grab.app.account.merchant.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Merchant Storefront instances.
 * Replaces legacy StorefrontController.
 */
public interface MerchantStorefrontPort {

    StorefrontMessageResult createStorefront(CreateStorefrontMessage message);

    StorefrontMessageResult updateStorefront(Id storefrontId, Id merchantId, UpdateStorefrontMessage message);

    void activateStorefront(Id storefrontId, Id merchantId);

    void suspendStorefront(Id storefrontId, Id merchantId, String reason);

    void reactivateStorefront(Id storefrontId, Id merchantId);

    void closeStorefront(Id storefrontId, Id merchantId);

    Optional<StorefrontMessageResult> findStorefrontById(Id storefrontId, Id merchantId);

    List<StorefrontMessageResult> listStorefrontsByMerchant(Id merchantId);

    record CreateStorefrontMessage(
            Id merchantId,
            String name,
            String slug
    ) {}

    record UpdateStorefrontMessage(
            String name,
            String slug
    ) {}

    record StorefrontMessageResult(
            String storefrontId,
            String merchantId,
            String name,
            String slug,
            String status,
            String lifecycleReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {}
}
