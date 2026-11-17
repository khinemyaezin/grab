package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface SellerAuthenticationPort extends Remote {
    VerifiedUser verify(Credentials message) throws RemoteException;

    AuthenticationState resolve(StateRequest message) throws RemoteException;

    record Credentials(String email, String password) implements Serializable {
        @Override
        public String toString() {
            return "Credentials[redacted]";
        }
    }

    record VerifiedUser(String userId, long authenticationVersion) implements Serializable {}

    record StateRequest(String userId, String assignmentId, boolean selectSingleContext) implements Serializable {}

    record AuthenticationState(String userId, String email, String status, long authenticationVersion,
                               String assignmentId, String scopeKey, String scopeId, Set<String> roles,
                               Set<String> authorities, long merchantRevision, long storefrontRevision)
            implements Serializable {}
}
