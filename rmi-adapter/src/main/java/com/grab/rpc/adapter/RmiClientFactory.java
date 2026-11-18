package com.grab.rpc.adapter;

import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;

public final class RmiClientFactory {

    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 30_000;

    private final Environment environment;

    public RmiClientFactory(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public <T extends Remote> T create(Class<T> contractType, String serviceKey) {
        Objects.requireNonNull(contractType, "contractType");
        String normalizedServiceKey = requireServiceKey(serviceKey);
        if (!Remote.class.isAssignableFrom(contractType)) {
            throw new IllegalArgumentException("RMI contract must extend Remote: " + contractType.getName());
        }

        String propertyPrefix = "grab.rpc.clients." + normalizedServiceKey;
        String host = requiredProperty(propertyPrefix + ".host");
        int port = requiredPort(propertyPrefix + ".port");
        int connectTimeoutMillis = property(propertyPrefix + ".connect-timeout-ms", "grab.rpc.connect-timeout-ms",
                DEFAULT_CONNECT_TIMEOUT_MILLIS);
        int readTimeoutMillis = property(propertyPrefix + ".read-timeout-ms", "grab.rpc.read-timeout-ms",
                DEFAULT_READ_TIMEOUT_MILLIS);
        validateTimeout("connect", connectTimeoutMillis);
        validateTimeout("read", readTimeoutMillis);

        RmiSocketFactory socketFactory = new RmiSocketFactory(host, connectTimeoutMillis, readTimeoutMillis);
        InvocationHandler invocationHandler = new RmiClientInvocationHandler(contractType, host, port, socketFactory);
        Object proxy = Proxy.newProxyInstance(
                contractType.getClassLoader(),
                new Class<?>[]{contractType},
                invocationHandler);
        return contractType.cast(proxy);
    }

    private String requireServiceKey(String serviceKey) {
        String normalizedServiceKey = Objects.requireNonNull(serviceKey, "serviceKey").trim();
        if (normalizedServiceKey.isEmpty()) {
            throw new IllegalArgumentException("RMI service key must not be blank");
        }
        return normalizedServiceKey;
    }

    private String requiredProperty(String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing RMI client property: " + propertyName);
        }
        return value.trim();
    }

    private int requiredPort(String propertyName) {
        Integer port = environment.getProperty(propertyName, Integer.class);
        if (port == null || port < 1 || port > 65_535) {
            throw new IllegalArgumentException("RMI client port must be between 1 and 65535: " + propertyName);
        }
        return port;
    }

    private int property(String specificPropertyName, String fallbackPropertyName, int defaultValue) {
        Integer specificValue = environment.getProperty(specificPropertyName, Integer.class);
        if (specificValue != null) {
            return specificValue;
        }
        return environment.getProperty(fallbackPropertyName, Integer.class, defaultValue);
    }

    private void validateTimeout(String name, int timeoutMillis) {
        if (timeoutMillis < 1) {
            throw new IllegalArgumentException("RMI " + name + " timeout must be positive");
        }
    }

    private static final class RmiClientInvocationHandler implements InvocationHandler {

        private final Class<?> contractType;
        private final String host;
        private final int port;
        private final RmiSocketFactory socketFactory;
        private Remote stub;

        private RmiClientInvocationHandler(
                Class<?> contractType,
                String host,
                int port,
                RmiSocketFactory socketFactory) {
            this.contractType = contractType;
            this.host = host;
            this.port = port;
            this.socketFactory = socketFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, method, arguments);
            }
            Remote remote = getStub();
            try {
                return method.invoke(remote, arguments);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    clearStub(remote);
                }
                throw cause;
            } catch (IllegalAccessException exception) {
                clearStub(remote);
                throw new RemoteException("Unable to invoke RMI contract method: " + method.getName(), exception);
            }
        }

        private synchronized Remote getStub() throws RemoteException {
            if (stub != null) {
                return stub;
            }
            try {
                Registry registry = LocateRegistry.getRegistry(host, port, socketFactory);
                stub = (Remote) registry.lookup(contractType.getName());
                return stub;
            } catch (NotBoundException exception) {
                throw new RemoteException("RMI contract is not bound: " + contractType.getName(), exception);
            } catch (RemoteException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new RemoteException("Unable to locate RMI contract: " + contractType.getName(), exception);
            }
        }

        private synchronized void clearStub(Remote failedStub) {
            if (stub == failedStub) {
                stub = null;
            }
        }

        private Object invokeObjectMethod(Object proxy, Method method, Object[] arguments) {
            return switch (method.getName()) {
                case "toString" -> "RmiClient(" + contractType.getName() + "@" + host + ":" + port + ")";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == arguments[0];
                default -> throw new IllegalStateException("Unsupported Object method: " + method.getName());
            };
        }
    }
}
