package com.grab.rpc.adapter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.security.GeneralSecurityException;

import com.grab.rpc.adapter.MutualTlsRmiEndpoint.SslMaterial;

public record MutualTlsRmiClient(int connectTimeoutMillis, int readTimeoutMillis)
        implements RMIClientSocketFactory, Serializable {
    private static volatile SSLContext clientContext;

    public static void configure(SslMaterial material) {
        try {
            clientContext = MutualTlsRmiContexts.create(material, false);
        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to configure authenticated RMI client", exception);
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            SSLContext context = clientContext;
            if (context == null) {
                throw new IOException("Authenticated RMI client has no configured TLS identity");
            }
            SSLSocket secure = (SSLSocket) context.getSocketFactory().createSocket(socket, host, port, true);
            secure.setSoTimeout(readTimeoutMillis);
            SSLParameters parameters = secure.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            secure.setSSLParameters(parameters);
            secure.startHandshake();
            return secure;
        } catch (IOException exception) {
            socket.close();
            throw new IOException("Unable to establish authenticated RMI transport", exception);
        }
    }

    public <T extends Remote> T lookup(String host, int port, Class<T> contract) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(host, port, this);
        try {
            Remote remote = registry.lookup(contract.getName());
            return contract.cast(remote);
        } catch (NotBoundException exception) {
            throw new RemoteException("Authentication RPC binding unavailable", exception);
        }
    }
}
