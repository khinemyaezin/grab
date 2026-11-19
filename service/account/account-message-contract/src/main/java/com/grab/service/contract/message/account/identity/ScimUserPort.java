package com.grab.service.contract.message.account.identity;

import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimUser;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ScimUserPort extends Remote {
    ScimUser provisionUser(ScimUser request) throws RemoteException;

    ScimUser getUser(String idOrExternalId) throws RemoteException;

    ScimUser updateUserStatus(String idOrExternalId, boolean active) throws RemoteException;

    ScimAccessContextExtension getUserAccessContexts(String idOrExternalId) throws RemoteException;
}
