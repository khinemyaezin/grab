package com.grab.service.sellerbff.internal.adapter.outbound.message;

import com.grab.rpc.adapter.MutualTlsRmiClient;
import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcProperties;

import java.rmi.Remote;

final class SellerAccountTransport {
    private final SellerOidcProperties properties;
    private final MutualTlsRmiClient client;
    private boolean initialized;

    SellerAccountTransport(SellerOidcProperties properties) {
        this.properties = properties;
        client = new MutualTlsRmiClient(Math.toIntExact(properties.accountConnectTimeout().toMillis()),
                Math.toIntExact(properties.accountReadTimeout().toMillis()));
    }

    synchronized <T extends Remote> T lookup(String host, int port, Class<T> contract) throws java.rmi.RemoteException {
        if (!initialized) {
            MutualTlsRmiClient.configure(new SslMaterial(properties.accountKeyStore(), properties.accountKeyPassword(),
                    properties.accountTrustStore(), properties.accountTrustPassword()));
            initialized = true;
        }
        return client.lookup(host, port, contract);
    }
}
