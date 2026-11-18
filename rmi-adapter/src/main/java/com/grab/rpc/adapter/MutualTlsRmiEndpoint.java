package com.grab.rpc.adapter;

import org.springframework.context.SmartLifecycle;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.util.Map;

public final class MutualTlsRmiEndpoint implements SmartLifecycle {
    private final String bindAddress;
    private final String advertisedHost;
    private final int port;
    private final SslMaterial material;
    private final Map<Class<? extends Remote>, Remote> providers;
    private Registry registry;
    private volatile boolean running;

    public MutualTlsRmiEndpoint(String bindAddress, String advertisedHost, int port, SslMaterial material,
                                Class<? extends Remote> contract, Remote provider) {
        this(bindAddress, advertisedHost, port, material, Map.of(contract, provider));
    }

    public MutualTlsRmiEndpoint(String bindAddress, String advertisedHost, int port, SslMaterial material,
                                Map<Class<? extends Remote>, Remote> providers) {
        this.bindAddress = bindAddress;
        this.advertisedHost = advertisedHost;
        this.port = port;
        this.material = material;
        this.providers = Map.copyOf(providers);
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        System.setProperty("java.rmi.server.hostname", advertisedHost);
        try {
            SSLContext context = MutualTlsRmiContexts.create(material, true);
            MutualTlsRmiClient client = new MutualTlsRmiClient(2000, 4000);
            RMIServerSocketFactory server = requestedPort -> {
                SSLServerSocket socket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket();
                socket.setNeedClientAuth(true);
                InetAddress address = InetAddress.getByName(bindAddress);
                socket.bind(new InetSocketAddress(address, requestedPort));
                return socket;
            };
            registry = LocateRegistry.createRegistry(port, client, server);
            ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                    "maxdepth=12;maxrefs=64;maxbytes=262144;java.lang.String;" +
                            "com.grab.service.contract.message.account.identity.*;!*");
            for (Map.Entry<Class<? extends Remote>, Remote> binding : providers.entrySet()) {
                Remote provider = binding.getValue();
                Remote stub = UnicastRemoteObject.exportObject(provider, port, client, server, filter);
                String name = binding.getKey().getName();
                registry.rebind(name, stub);
            }
            running = true;
        } catch (GeneralSecurityException | IOException exception) {
            stop();
            throw new IllegalStateException("Unable to start authenticated RMI listener", exception);
        }
    }

    @Override
    public synchronized void stop() {
        providers.values().forEach(this::unexport);
        if (registry != null) {
            unexport(registry);
            registry = null;
        }
        running = false;
    }

    private void unexport(Remote remote) {
        try {
            UnicastRemoteObject.unexportObject(remote, true);
        } catch (NoSuchObjectException ignored) {
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public record SslMaterial(Path keyStore, String keyPassword, Path peerTrustStore, String trustPassword) {
        @Override
        public String toString() {
            return "SslMaterial[redacted]";
        }
    }
}
