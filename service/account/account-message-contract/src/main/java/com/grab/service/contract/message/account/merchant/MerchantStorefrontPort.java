package com.grab.service.contract.message.account.merchant;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;

/**
 * Universal Java Port Interface for Merchant Storefront instances.
 * Replaces legacy StorefrontController.
 */
public interface MerchantStorefrontPort extends Remote {

    StorefrontMessageResult createStorefront(CreateStorefrontMessage message) throws RemoteException;

    StorefrontMessageResult updateStorefront(Id storefrontId, Id merchantId, UpdateStorefrontMessage message) throws RemoteException;

    void activateStorefront(Id storefrontId, Id merchantId) throws RemoteException;

    void suspendStorefront(Id storefrontId, Id merchantId, String reason) throws RemoteException;

    void reactivateStorefront(Id storefrontId, Id merchantId) throws RemoteException;

    void closeStorefront(Id storefrontId, Id merchantId) throws RemoteException;

    StorefrontMessageResult findStorefrontById(Id storefrontId, Id merchantId) throws RemoteException;

    List<StorefrontMessageResult> listStorefrontsByMerchant(Id merchantId) throws RemoteException;

    record CreateStorefrontMessage(
            Id merchantId,
            String name,
            String slug
    ) implements Serializable {}

    record UpdateStorefrontMessage(
            String name,
            String slug
    ) implements Serializable {}

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
    ) implements Serializable {}
}
