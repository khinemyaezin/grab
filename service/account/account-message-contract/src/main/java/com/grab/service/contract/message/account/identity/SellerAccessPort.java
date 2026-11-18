package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface SellerAccessPort extends Remote {
    AccessState resolve(AccessRequest request) throws RemoteException;

    enum Intent { INITIAL, REVALIDATE, SELECT }

    enum Status { ACTIVE, UNKNOWN_IDENTITY, INACTIVE_IDENTITY, INVALID_CONTEXT, DENIED }

    record AccessRequest(String issuer, String subject, String assignmentId, Intent intent) implements Serializable {}

    record AccessState(Status status, String userId, String email, long authenticationVersion,
                       String assignmentId, String scopeKey, String scopeId, Set<String> roles,
                       Set<String> authorities, long merchantRevision, long storefrontRevision)
            implements Serializable {
        public AccessState {
            roles = roles == null ? Set.of() : Set.copyOf(roles);
            authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
        }
    }
}
