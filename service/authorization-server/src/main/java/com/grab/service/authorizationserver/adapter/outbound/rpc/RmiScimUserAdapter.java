package com.grab.service.authorizationserver.adapter.outbound.rpc;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.authorizationserver.config.AuthorizationIssuerProperties;
import com.grab.service.authorizationserver.port.outbound.ScimClientPort;
import com.grab.service.contract.message.account.identity.ScimUserPort;
import com.grab.service.contract.message.account.scim.ScimAccessContextExtension;
import com.grab.service.contract.message.account.scim.ScimUser;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;

@Component
public class RmiScimUserAdapter implements ScimClientPort {
    private static final Logger LOG = Loggers.getLogger(RmiScimUserAdapter.class);
    private final AuthorizationIssuerProperties properties;
    private final MutualTlsRmiClient transport;

    public RmiScimUserAdapter(AuthorizationIssuerProperties properties) {
        this.properties = properties;
        this.transport = new MutualTlsRmiClient(properties.accountConnectTimeout(), properties.accountReadTimeout());
        SslMaterial tls = new SslMaterial(properties.accountClientKeyStore(), properties.accountClientKeyPassword(),
                properties.accountTrustStore(), properties.accountTrustPassword());
        MutualTlsRmiClient.configure(tls);
    }

    @Override
    public ScimUser provisionUser(ScimUser request) {
        try {
            return scimUserPort().provisionUser(request);
        } catch (RemoteException | RuntimeException e) {
            LOG.warn("Failed to provision user over SCIM RPC: {}", e.getMessage());
            throw new RuntimeException("SCIM provisioning failed", e);
        }
    }

    @Override
    public ScimUser getUser(String idOrExternalId) {
        try {
            return scimUserPort().getUser(idOrExternalId);
        } catch (RemoteException | RuntimeException e) {
            LOG.warn("Failed to get user over SCIM RPC: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ScimUser updateUserStatus(String idOrExternalId, boolean active) {
        try {
            return scimUserPort().updateUserStatus(idOrExternalId, active);
        } catch (RemoteException | RuntimeException e) {
            LOG.warn("Failed to update user status over SCIM RPC: {}", e.getMessage());
            throw new RuntimeException("SCIM update status failed", e);
        }
    }

    @Override
    public ScimAccessContextExtension getUserAccessContexts(String idOrExternalId) {
        try {
            return scimUserPort().getUserAccessContexts(idOrExternalId);
        } catch (RemoteException | RuntimeException e) {
            LOG.warn("Failed to get user access contexts over SCIM RPC: {}", e.getMessage());
            return null;
        }
    }

    private ScimUserPort scimUserPort() throws RemoteException {
        return transport.lookup(properties.accountHost(), properties.accountPort(), ScimUserPort.class);
    }
}
