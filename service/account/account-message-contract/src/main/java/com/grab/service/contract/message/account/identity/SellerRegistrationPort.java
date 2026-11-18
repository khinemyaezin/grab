package com.grab.service.contract.message.account.identity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

public interface SellerRegistrationPort extends Remote {
    RegistrationResult registerUser(RegisterUserRequest request)
            throws RemoteException;

    record RegisterUserRequest(String email, String password) implements Serializable {
        @Override
        public String toString() {
            return "RegisterUserRequest[redacted]";
        }
    }

    record RegistrationResult(RegistrationStatus status, String id, String email, String userStatus, String createdAt)
            implements Serializable {}

    enum RegistrationStatus { CREATED, EMAIL_EXISTS, INVALID_INPUT }
}
