package com.grab.service.contract.message.account.identity;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SellerAuthenticationPort extends Remote {
    VerifiedUser verify(Credentials message) throws RemoteException;

    record Credentials(String email, String password) implements Serializable {
        @Override
        public String toString() {
            return "Credentials[redacted]";
        }
    }

    record VerifiedUser(String userId, long authenticationVersion) implements Serializable {}

}
