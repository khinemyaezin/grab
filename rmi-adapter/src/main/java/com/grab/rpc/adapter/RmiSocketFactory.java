package com.grab.rpc.adapter;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Objects;

public final class RmiSocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String bindAddress;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public RmiSocketFactory(String bindAddress, int connectTimeoutMillis, int readTimeoutMillis) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
        socket.setSoTimeout(readTimeoutMillis);
        return socket;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        InetAddress address = InetAddress.getByName(bindAddress);
        return new ServerSocket(port, 50, address);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RmiSocketFactory that)) {
            return false;
        }
        return bindAddress.equals(that.bindAddress)
                && connectTimeoutMillis == that.connectTimeoutMillis
                && readTimeoutMillis == that.readTimeoutMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindAddress, connectTimeoutMillis, readTimeoutMillis);
    }
}
