package com.grab.service.contract.message.account.customer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.Instant;

/**
 * Universal Java Port Interface for Customer Profile.
 */
public interface CustomerAccountPort extends Remote {

    CustomerProfileResult getCustomerById(Id customerId) throws RemoteException;

    CustomerProfileResult getCustomerByUserId(Id userId) throws RemoteException;

    record CustomerProfileResult(
            String id,
            String userId,
            String email,
            String displayName,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) implements Serializable {}
}
