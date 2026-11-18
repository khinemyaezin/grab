package com.grab.rpc.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "grab.rpc", name = "enabled", havingValue = "true")
public class RmiProviderConfiguration {

    @Bean
    public RmiProviderLifecycle rmiProviderLifecycle(
            ApplicationContext applicationContext,
            RmiProviderProperties properties) {
        return new RmiProviderLifecycle(applicationContext, properties);
    }

    @Bean
    public RmiProviderProperties rmiProviderProperties(
            Environment environment) {
        int registryPort = environment.getProperty("grab.rpc.registry-port", Integer.class, 1099);
        String bindAddress = environment.getProperty("grab.rpc.bind-address", "127.0.0.1");
        String advertisedHost = environment.getProperty("grab.rpc.advertised-host", "localhost");
        int connectTimeoutMillis = environment.getProperty("grab.rpc.connect-timeout-ms", Integer.class, 3_000);
        int readTimeoutMillis = environment.getProperty("grab.rpc.read-timeout-ms", Integer.class, 30_000);
        return new RmiProviderProperties(
                registryPort,
                bindAddress,
                advertisedHost,
                connectTimeoutMillis,
                readTimeoutMillis);
    }
}
