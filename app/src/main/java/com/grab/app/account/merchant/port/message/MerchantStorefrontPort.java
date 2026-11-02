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

    StorefrontResult createStorefront(CreateStorefrontMessage message);

    StorefrontResult updateStorefront(Id storefrontId, UpdateStorefrontMessage message);

    void activateStorefront(Id storefrontId);

    void suspendStorefront(Id storefrontId, String reason);

    void reactivateStorefront(Id storefrontId);

    void closeStorefront(Id storefrontId);

    Optional<StorefrontResult> findStorefrontById(Id storefrontId);

    List<StorefrontResult> listStorefrontsByMerchant(Id merchantId);

    record CreateStorefrontMessage(
            Id merchantId,
            String name,
            String domainUrl,
            Id defaultSalesChannelId
    ) {}

    record UpdateStorefrontMessage(
            String name,
            String domainUrl
    ) {}

    record StorefrontResult(
            Id id,
            Id merchantId,
            String name,
            String domainUrl,
            Id defaultSalesChannelId,
            String status,
            Instant createdAt
    ) {}
}

