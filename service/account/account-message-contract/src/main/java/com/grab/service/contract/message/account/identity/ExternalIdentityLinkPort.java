package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ExternalIdentityLinkPort extends Remote {
    LinkResult linkExistingIdentity(LinkRequest request) throws RemoteException;

    record LinkRequest(String platformUserId, String issuer, String subject, String directoryId, String resourceId,
                       String actorId) implements Serializable {
    }

    record LinkResult(String platformUserId, String issuer, String subject, boolean alreadyLinked)
            implements Serializable {
    }
}
