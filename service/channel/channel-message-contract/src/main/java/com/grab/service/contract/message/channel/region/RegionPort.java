package com.grab.service.contract.message.channel.region;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Set;

/**
 * Universal Java Port Interface for Geographic Regions, Currencies, and Jurisdictions.
 * Unifies RegionController and RegionQuery.
 */
public interface RegionPort extends Remote {

    RegionMessageResult findRegionById(Id regionId) throws RemoteException;

    RegionMessageResult findRegionByCountryCode(String countryCode) throws RemoteException;

    List<String> listSupportedCountryCodes() throws RemoteException;

    record RegionMessageResult(
            String regionId,
            String name,
            String currencyCode,
            String status,
            Set<String> countryCodes
    ) implements Serializable {
        public boolean active() {
            return "ACTIVE".equalsIgnoreCase(status);
        }
    }
}
