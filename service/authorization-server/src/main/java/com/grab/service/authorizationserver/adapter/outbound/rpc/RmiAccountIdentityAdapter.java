package com.grab.service.authorizationserver.adapter.outbound.rpc;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.authorizationserver.config.SellerIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.Credentials;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.StateRequest;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.VerifiedUser;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public class RmiAccountIdentityAdapter implements AccountIdentityPort {
    private static final Logger LOG = Loggers.getLogger(RmiAccountIdentityAdapter.class);
    private final SellerIssuerProperties properties;
    private final MutualTlsRmiClient transport;

    public RmiAccountIdentityAdapter(SellerIssuerProperties properties) {
        this.properties = properties;
        this.transport = new MutualTlsRmiClient(properties.accountConnectTimeout(), properties.accountReadTimeout());
        SslMaterial tls = new SslMaterial(properties.accountClientKeyStore(), properties.accountClientKeyPassword(),
                properties.accountTrustStore(), properties.accountTrustPassword());
        MutualTlsRmiClient.configure(tls);
    }

    @Override
    public VerifiedUser verify(String email, String password) {
        try {
            SellerAuthenticationPort account = connect();
            return account.verify(new Credentials(email, password));
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public AuthenticationState resolve(String userId, String assignmentId, boolean selectSingleContext) {
        try {
            SellerAuthenticationPort account = connect();
            return account.resolve(new StateRequest(userId, assignmentId, selectSingleContext));
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    private AccountUnavailableException unavailable(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String failure = cause.getClass().getSimpleName();
        LOG.warn("Account authentication RPC failed cause={}", failure);
        return new AccountUnavailableException("Account authentication is unavailable", exception);
    }

    private SellerAuthenticationPort connect() throws RemoteException {
        return transport.lookup(properties.accountHost(), properties.accountPort(), SellerAuthenticationPort.class);
    }
}
