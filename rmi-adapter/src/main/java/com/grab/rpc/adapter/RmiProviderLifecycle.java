package com.grab.rpc.adapter;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.SmartLifecycle;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class RmiProviderLifecycle implements SmartLifecycle {

    private final ListableBeanFactory beanFactory;
    private final RmiProviderProperties properties;
    private final Map<String, Remote> exportedProviders = new LinkedHashMap<>();
    private Registry registry;
    private boolean running;

    public RmiProviderLifecycle(ListableBeanFactory beanFactory, RmiProviderProperties properties) {
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        Map<String, Remote> providers = new LinkedHashMap<>();
        for (Map.Entry<String, Remote> entry : beanFactory.getBeansOfType(Remote.class).entrySet()) {
            if (beanFactory.findAnnotationOnBean(entry.getKey(), RmiExport.class) != null) {
                providers.put(entry.getKey(), entry.getValue());
            }
        }
        if (providers.isEmpty()) {
            throw new IllegalStateException("RMI is enabled but no Remote provider beans were found");
        }

        System.setProperty("java.rmi.server.hostname", properties.advertisedHost());
        Map<String, Remote> providersByContract = providersByContract(providers);
        try {
            RmiSocketFactory socketFactory = new RmiSocketFactory(
                    properties.bindAddress(),
                    properties.connectTimeoutMillis(),
                    properties.readTimeoutMillis());
            registry = LocateRegistry.createRegistry(properties.registryPort(), socketFactory, socketFactory);
            for (Map.Entry<String, Remote> entry : providersByContract.entrySet()) {
                Remote provider = entry.getValue();
                Remote stub = UnicastRemoteObject.exportObject(provider, properties.registryPort(), socketFactory,
                        socketFactory);
                exportedProviders.put(entry.getKey(), provider);
                registry.rebind(entry.getKey(), stub);
            }
            running = true;
        } catch (Exception exception) {
            stop();
            throw new IllegalStateException("Unable to start RMI providers", exception);
        }
    }

    private Map<String, Remote> providersByContract(Map<String, Remote> providers) {
        Map<String, Remote> providersByContract = new LinkedHashMap<>();
        for (Remote provider : providers.values()) {
            Class<?> contract = findRemoteContract(provider);
            Remote previousProvider = providersByContract.putIfAbsent(contract.getName(), provider);
            if (previousProvider != null) {
                throw new IllegalStateException("Duplicate RMI contract binding: " + contract.getName());
            }
        }
        return providersByContract;
    }

    private Class<?> findRemoteContract(Remote provider) {
        Set<Class<?>> remoteContracts = new LinkedHashSet<>();
        collectRemoteContracts(provider.getClass(), remoteContracts);
        if (remoteContracts.size() != 1) {
            throw new IllegalStateException("Each RMI provider must implement exactly one Remote contract: "
                    + provider.getClass().getName());
        }
        return remoteContracts.iterator().next();
    }

    private void collectRemoteContracts(Class<?> type, Set<Class<?>> remoteContracts) {
        if (type == null) {
            return;
        }
        for (Class<?> implementedInterface : type.getInterfaces()) {
            if (implementedInterface != Remote.class && Remote.class.isAssignableFrom(implementedInterface)) {
                remoteContracts.add(implementedInterface);
            }
            collectRemoteContracts(implementedInterface, remoteContracts);
        }
        collectRemoteContracts(type.getSuperclass(), remoteContracts);
    }

    @Override
    public synchronized void stop() {
        for (Map.Entry<String, Remote> entry : exportedProviders.entrySet()) {
            try {
                if (registry != null) {
                    registry.unbind(entry.getKey());
                }
            } catch (Exception ignored) {
                // The registry may already be shutting down.
            }
            try {
                UnicastRemoteObject.unexportObject(entry.getValue(), true);
            } catch (Exception ignored) {
                // The provider may already have been unexported.
            }
        }
        exportedProviders.clear();
        if (registry != null) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
            } catch (Exception ignored) {
                // The registry may already have been unexported.
            }
            registry = null;
        }
        running = false;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
