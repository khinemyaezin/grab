package com.grab.service.contract.message.channel.saleschannel;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Sales Channel management.
 * Replaces legacy SalesChannelController.
 */
public interface SalesChannelManagementPort extends Remote {

    ProvisionResult provisionMerchantWebsiteChannel(Id merchantId, String countryCode) throws RemoteException;

    ChannelMessageResult findChannelById(String salesChannelId, String merchantId) throws RemoteException;

    List<ChannelMessageResult> listChannels(String merchantId, int page, int size) throws RemoteException;

    record ProvisionResult(
            Id salesChannelId,
            boolean created
    ) implements Serializable {}

    record ChannelMessageResult(
            String salesChannelId,
            String name,
            String type,
            String owner,
            String merchantId,
            String status
    ) implements Serializable {}
}
