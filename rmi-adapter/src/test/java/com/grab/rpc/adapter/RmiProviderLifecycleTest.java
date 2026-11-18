package com.grab.rpc.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RmiProviderLifecycleTest {

    @Test
    void twoSpringApplicationsCommunicateThroughMultipleContractsOnOnePort() throws Exception {
        int registryPort = availablePort();
        try (AnnotationConfigApplicationContext providerContext = providerContext(registryPort);
             AnnotationConfigApplicationContext consumerContext = consumerContext(registryPort)) {
            EchoPort echoClient = consumerContext.getBean(EchoPort.class);
            UppercasePort uppercaseClient = consumerContext.getBean(UppercasePort.class);

            EchoMessage echoResult = echoClient.echo(new EchoMessage("rpc-round-trip"));
            EchoMessage uppercaseResult = uppercaseClient.uppercase(new EchoMessage("rpc-round-trip"));

            assertThat(echoResult.value()).isEqualTo("rpc-round-trip");
            assertThat(uppercaseResult.value()).isEqualTo("RPC-ROUND-TRIP");
            assertThat(registryBindings(registryPort)).contains(EchoPort.class.getName(), UppercasePort.class.getName());
            assertThat(registryBindings(registryPort)).doesNotContain(ClientOnlyPort.class.getName());
        }
    }

    @Test
    void clientConnectsAfterProviderStartsAndReconnectsAfterProviderRestart() throws Exception {
        int registryPort = availablePort();
        try (AnnotationConfigApplicationContext consumerContext = consumerContext(registryPort)) {
            EchoPort echoClient = consumerContext.getBean(EchoPort.class);

            assertThatThrownBy(() -> echoClient.echo(new EchoMessage("before-start")))
                    .isInstanceOf(RemoteException.class);

            try (AnnotationConfigApplicationContext firstProviderContext = providerContext(registryPort)) {
                assertThat(echoClient.echo(new EchoMessage("first-provider")).value())
                        .isEqualTo("first-provider");
            }

            assertThatThrownBy(() -> echoClient.echo(new EchoMessage("after-stop")))
                    .isInstanceOf(RemoteException.class);

            try (AnnotationConfigApplicationContext secondProviderContext = providerContext(registryPort)) {
                assertThat(echoClient.echo(new EchoMessage("second-provider")).value())
                        .isEqualTo("second-provider");
            }
        }
    }

    @Test
    void duplicateContractsAreRejectedBeforeRegistryStarts() throws Exception {
        int registryPort = availablePort();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("firstEchoProvider", new EchoProvider());
        beanFactory.registerSingleton("secondEchoProvider", new EchoProvider());
        RmiProviderLifecycle lifecycle = new RmiProviderLifecycle(beanFactory, providerProperties(registryPort));

        assertThatThrownBy(lifecycle::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate RMI contract binding");
        assertThatThrownBy(() -> registryBindings(registryPort))
                .isInstanceOf(RemoteException.class);
    }

    @Test
    void onlyMarkedProvidersAreExported() throws Exception {
        int registryPort = availablePort();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("echoProvider", new EchoProvider());
        beanFactory.registerSingleton("clientOnly", new ClientOnly());
        RmiProviderLifecycle lifecycle = new RmiProviderLifecycle(beanFactory, providerProperties(registryPort));

        lifecycle.start();
        try {
            assertThat(registryBindings(registryPort)).contains(EchoPort.class.getName());
            assertThat(registryBindings(registryPort)).doesNotContain(ClientOnlyPort.class.getName());
        } finally {
            lifecycle.stop();
        }
    }

    @Test
    void invalidPropertiesAndMissingClientEndpointFailFast() {
        assertThatThrownBy(() -> new RmiProviderProperties(0, "127.0.0.1", "localhost", 3_000, 30_000))
                .isInstanceOf(IllegalArgumentException.class);

        RmiClientFactory clientFactory = new RmiClientFactory(new MockEnvironment());
        assertThatThrownBy(() -> clientFactory.create(EchoPort.class, "provider"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grab.rpc.clients.provider.host");
    }

    @Test
    void readTimeoutIsAppliedToRemoteCalls() throws Exception {
        int registryPort = availablePort();
        try (AnnotationConfigApplicationContext providerContext = providerContext(registryPort, 3_000, 100);
             AnnotationConfigApplicationContext consumerContext = consumerContext(registryPort, 3_000, 100)) {
            SlowPort slowClient = consumerContext.getBean(SlowPort.class);

            assertThatThrownBy(() -> slowClient.slow(500))
                    .isInstanceOf(RemoteException.class);
        }
    }

    private AnnotationConfigApplicationContext providerContext(int registryPort) {
        return providerContext(registryPort, 3_000, 30_000);
    }

    private AnnotationConfigApplicationContext providerContext(
            int registryPort,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        addProperties(context, registryPort, connectTimeoutMillis, readTimeoutMillis, true);
        context.registerBean(EchoProvider.class);
        context.registerBean(UppercaseProvider.class);
        context.registerBean(SlowProvider.class);
        context.register(RmiProviderConfiguration.class);
        context.refresh();
        return context;
    }

    private AnnotationConfigApplicationContext consumerContext(int registryPort) {
        return consumerContext(registryPort, 3_000, 30_000);
    }

    private AnnotationConfigApplicationContext consumerContext(
            int registryPort,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        addProperties(context, registryPort, connectTimeoutMillis, readTimeoutMillis, false);
        context.registerBean(ConsumerConfiguration.class);
        context.register(RmiClientConfiguration.class);
        context.refresh();
        return context;
    }

    private void addProperties(
            AnnotationConfigApplicationContext context,
            int registryPort,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean providerEnabled) {
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                "test-rmi",
                Map.of(
                        "grab.rpc.enabled", Boolean.toString(providerEnabled),
                        "grab.rpc.registry-port", Integer.toString(registryPort),
                        "grab.rpc.bind-address", "127.0.0.1",
                        "grab.rpc.advertised-host", "127.0.0.1",
                        "grab.rpc.connect-timeout-ms", Integer.toString(connectTimeoutMillis),
                        "grab.rpc.read-timeout-ms", Integer.toString(readTimeoutMillis),
                        "grab.rpc.clients.provider.host", "127.0.0.1",
                        "grab.rpc.clients.provider.port", Integer.toString(registryPort))));
    }

    private RmiProviderProperties providerProperties(int registryPort) {
        return new RmiProviderProperties(registryPort, "127.0.0.1", "127.0.0.1", 3_000, 30_000);
    }

    private String[] registryBindings(int registryPort) throws Exception {
        RmiSocketFactory socketFactory = new RmiSocketFactory("127.0.0.1", 3_000, 30_000);
        return LocateRegistry.getRegistry("127.0.0.1", registryPort, socketFactory).list();
    }

    private int availablePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerConfiguration {

        @Bean
        EchoPort echoPort(RmiClientFactory factory) {
            return factory.create(EchoPort.class, "provider");
        }

        @Bean
        UppercasePort uppercasePort(RmiClientFactory factory) {
            return factory.create(UppercasePort.class, "provider");
        }

        @Bean
        SlowPort slowPort(RmiClientFactory factory) {
            return factory.create(SlowPort.class, "provider");
        }
    }

    @RmiExport
    static class EchoProvider implements EchoPort {
        @Override
        public EchoMessage echo(EchoMessage message) {
            return message;
        }
    }

    @RmiExport
    static class UppercaseProvider implements UppercasePort {
        @Override
        public EchoMessage uppercase(EchoMessage message) {
            return new EchoMessage(message.value().toUpperCase());
        }
    }

    @RmiExport
    static class SlowProvider implements SlowPort {
        @Override
        public void slow(long delayMillis) throws RemoteException {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new RemoteException("Slow provider interrupted", exception);
            }
        }
    }

    static class ClientOnly implements ClientOnlyPort {
        @Override
        public void call() {
        }
    }

    interface EchoPort extends Remote {
        EchoMessage echo(EchoMessage message) throws RemoteException;
    }

    interface UppercasePort extends Remote {
        EchoMessage uppercase(EchoMessage message) throws RemoteException;
    }

    interface SlowPort extends Remote {
        void slow(long delayMillis) throws RemoteException;
    }

    interface ClientOnlyPort extends Remote {
        void call() throws RemoteException;
    }

    record EchoMessage(String value) implements Serializable {
    }
}
