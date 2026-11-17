package com.grab.service.account.internal.config;

import com.grab.rpc.adapter.RmiClientConfiguration;
import com.grab.rpc.adapter.RmiProviderConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        AccountDataSourceConfig.class,
        AccountBusConfig.class,
        RmiClientConfiguration.class,
        RmiProviderConfiguration.class
})
public class AccountConfig {
}

