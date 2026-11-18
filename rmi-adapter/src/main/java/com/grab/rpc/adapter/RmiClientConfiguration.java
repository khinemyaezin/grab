package com.grab.rpc.adapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
public class RmiClientConfiguration {

    @Bean
    public RmiClientFactory rmiClientFactory(Environment environment) {
        return new RmiClientFactory(environment);
    }
}
