package com.grab.service.contract.message.channel.saleschannel;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;

/**
 * Universal Java Port Interface for Sales Channel Context and Region Resolution.
 * Unifies legacy SalesChannelPort and ChannelRegionQueryPort.
 */
public interface SalesChannelContextPort extends Remote {

    SalesChannelSliceResult findChannelSlice(Id salesChannelId) throws RemoteException;

    boolean isEnabled(Id salesChannelId) throws RemoteException;

    List<Id> findRegionsForChannel(Id salesChannelId) throws RemoteException;

    record SalesChannelSliceResult(
            String salesChannelId,
            String channelType,
            String status,
            String merchantId
    ) implements Serializable {
        public boolean enabled() {
            return "ENABLED".equalsIgnoreCase(status);
        }

        public boolean website() {
            return "WEBSITE".equalsIgnoreCase(channelType);
        }

        public boolean marketplace() {
            return "MARKETPLACE".equalsIgnoreCase(channelType);
        }
    }
}
