package com.grab.service.authorizationserver.adapter.outbound.rpc;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.authorizationserver.config.AuthorizationIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.AccountIdentityPort;
import com.grab.service.authorizationserver.port.outbound.AssignmentIdentityPort;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.Credentials;
import com.grab.service.contract.message.account.identity.AccountAuthenticationPort.SubjectRequest;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.AuthenticationState;
import com.grab.service.contract.message.account.identity.SellerAuthenticationPort.StateRequest;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public class RmiAccountIdentityAdapter implements AccountIdentityPort, AssignmentIdentityPort {
    private static final Logger LOG = Loggers.getLogger(RmiAccountIdentityAdapter.class);
    private final AuthorizationIssuerProperties properties;
    private final MutualTlsRmiClient transport;

    public RmiAccountIdentityAdapter(AuthorizationIssuerProperties properties) {
        this.properties = properties;
        this.transport = new MutualTlsRmiClient(properties.accountConnectTimeout(), properties.accountReadTimeout());
        SslMaterial tls = new SslMaterial(properties.accountClientKeyStore(), properties.accountClientKeyPassword(),
                properties.accountTrustStore(), properties.accountTrustPassword());
        MutualTlsRmiClient.configure(tls);
    }

    @Override
    public Subject verify(String email, String password) {
        try {
            AccountAuthenticationPort account = account();
            Credentials request = new Credentials(email, password);
            AccountAuthenticationPort.Subject subject = account.verify(request);
            return toSubject(subject);
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public Subject resolve(String userId) {
        try {
            AccountAuthenticationPort account = account();
            SubjectRequest request = new SubjectRequest(userId);
            AccountAuthenticationPort.Subject subject = account.resolve(request);
            return toSubject(subject);
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public State resolve(String userId, String assignmentId, boolean selectSingleContext) {
        try {
            SellerAuthenticationPort assignments = transport.lookup(properties.accountHost(), properties.accountPort(),
                    SellerAuthenticationPort.class);
            StateRequest request = new StateRequest(userId, assignmentId, selectSingleContext);
            AuthenticationState state = assignments.resolve(request);
            if (state == null) {
                return null;
            }
            Subject subject = new Subject(state.userId(), state.authenticationVersion());
            return new State(subject, state.email(), state.assignmentId(), state.scopeKey(), state.scopeId(),
                    state.roles(), state.authorities(), state.merchantRevision(), state.storefrontRevision());
        } catch (RemoteException | RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    private AccountAuthenticationPort account() throws RemoteException {
        return transport.lookup(properties.accountHost(), properties.accountPort(), AccountAuthenticationPort.class);
    }

    private static Subject toSubject(AccountAuthenticationPort.Subject subject) {
        return subject == null ? null : new Subject(subject.userId(), subject.authenticationVersion());
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

}
