package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AccountAuthenticationPort extends Remote {
    @Deprecated(since = "2.0", forRemoval = true)
    Subject verify(Credentials request) throws RemoteException;

    Subject resolve(SubjectRequest request) throws RemoteException;

    record Credentials(String email, String password) implements Serializable {
        @Override
        public String toString() {
            return "Credentials[redacted]";
        }
    }

    record SubjectRequest(String userId) implements Serializable {}

    record Subject(String userId, long authenticationVersion) implements Serializable {}
}
