package com.grab.service.authorizationserver;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.Credentials;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.StateRequest;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
class SellerAccountAuthentication {
    private static final Logger LOG = Loggers.getLogger(SellerAccountAuthentication.class);
    private final SellerIssuerProperties properties;
    private final MutualTlsRmiClient transport = new MutualTlsRmiClient(1500, 3000);

    SellerAccountAuthentication(SellerIssuerProperties properties) {
        this.properties = properties;
        SslMaterial tls = new SslMaterial(properties.accountClientKeyStore(), properties.accountClientKeyPassword(),
                properties.accountTrustStore(), properties.accountTrustPassword());
        MutualTlsRmiClient.configure(tls);
    }

    VerifiedUser verify(String email, String password) {
        try {
            SellerAuthenticationPort account = connect();
            return account.verify(new Credentials(email, password));
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    AuthenticationState resolve(String userId, String assignmentId, boolean selectSingleContext) {
        try {
            SellerAuthenticationPort account = connect();
            return account.resolve(new StateRequest(userId, assignmentId, selectSingleContext));
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    private AccountUnavailable unavailable(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String failure = cause.getClass().getSimpleName();
        LOG.warn("Account authentication RPC failed cause={}", failure);
        return new AccountUnavailable(exception);
    }

    private SellerAuthenticationPort connect() throws RemoteException {
        return transport.lookup(properties.accountHost(), properties.accountPort(), SellerAuthenticationPort.class);
    }

    static final class AccountUnavailable extends RuntimeException {
        AccountUnavailable(Throwable cause) {
            super("Account authentication is unavailable", cause);
        }
    }
}
