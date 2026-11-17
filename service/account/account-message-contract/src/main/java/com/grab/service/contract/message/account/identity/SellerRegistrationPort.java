package com.grab.service.contract.message.account.identity;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SellerRegistrationPort extends Remote {
    AuthenticationPort.UserRegistrationResult registerUser(AuthenticationPort.RegisterUserMessage message)
            throws RemoteException;
}
